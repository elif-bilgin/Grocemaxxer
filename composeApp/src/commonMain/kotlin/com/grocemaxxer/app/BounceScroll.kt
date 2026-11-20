package com.grocemaxxer.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import kotlin.math.abs
import kotlin.math.sign

/** The pull can approach, but never reach, this many pixels. */
private const val PULL_LIMIT_PX = 140f

/** Ceiling on stored drag, which bounds how far back a finger has to travel. */
private const val MAX_DRAG_PX = 420f

/**
 * A rubber-band bounce for the ends of a scrolling list.
 *
 * Compose Multiplatform 1.7 has no way to hand a custom [androidx.compose.foundation.OverscrollEffect]
 * to a `LazyColumn` -- neither `LazyColumn` nor `Modifier.scrollable` takes one at
 * this version, and `Modifier.overscroll` only *draws* an effect without ever
 * feeding it scroll deltas. So the bounce is built on nested scroll instead,
 * which is stable API here.
 *
 * Drag overscroll is this bounce; fling overscroll is deliberately left to the
 * platform. See [NestedScrollConnection.onPostScroll] below for why.
 */
class BounceScrollState {
    /** Pixels the list is currently displaced by; positive is downward. */
    var pull by mutableFloatStateOf(0f)
        private set

    /** Raw finger travel past the end, before the rubber band compresses it. */
    private var drag = 0f

    /**
     * Compresses raw drag onto an asymptote at [PULL_LIMIT_PX]. Tracking raw
     * travel rather than the compressed value is what makes winding and
     * unwinding symmetric: the list follows the finger back out at exactly the
     * rate it followed it in, instead of leaving dead travel to pay off first.
     */
    private fun rubberBand(raw: Float): Float =
        sign(raw) * (1f - 1f / (abs(raw) / PULL_LIMIT_PX + 1f)) * PULL_LIMIT_PX

    private fun applyDrag(delta: Float) {
        drag = (drag + delta).coerceIn(-MAX_DRAG_PX, MAX_DRAG_PX)
        pull = rubberBand(drag)
    }

    private suspend fun settle() {
        drag = 0f
        if (pull == 0f) return
        // Cancelled if the user touches down again mid-spring, which leaves
        // `pull` wherever the animation got to -- where the next drag resumes.
        Animatable(pull).animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        ) { pull = value }
        pull = 0f
    }

    val connection = object : NestedScrollConnection {
        /** Scrolling back toward the content unwinds the pull before the list moves. */
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (drag == 0f || available.y == 0f || sign(available.y) == sign(drag)) {
                return Offset.Zero
            }
            val next = drag + available.y
            return if (sign(next) != sign(drag)) {
                // The pull is used up; hand the rest of the gesture to the list.
                val spent = -drag
                drag = 0f
                pull = 0f
                Offset(0f, spent)
            } else {
                applyDrag(available.y)
                Offset(0f, available.y)
            }
        }

        /**
         * Whatever the list could not scroll is at its end, so it becomes pull --
         * but only under a finger.
         *
         * Leftover from a *fling* is deliberately left unconsumed. Compose cancels
         * a fling only when the scroll declines to consume what it offered, so
         * taking it here keeps the decay animation running against the end of the
         * list for its full duration, which on a fast fling is a second or more of
         * the list sitting pulled open before the spring is allowed to start.
         * That was the delayed, out-of-nowhere bounce.
         *
         * Declining it hands fling overscroll to the platform effect instead --
         * on Android, the stock stretch. Suppressing that to bounce on flings too
         * would need `LocalOverscrollConfiguration`, which is not reachable from
         * common code, so the two gestures differ by one effect and neither
         * glitches. Consuming the finger's leftover above still starves the
         * stretch during a drag, so the two never run at once.
         */
        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset {
            if (available.y == 0f || source != NestedScrollSource.UserInput) return Offset.Zero
            applyDrag(available.y)
            return Offset(0f, available.y)
        }

        /** Released while overscrolled: spring back instead of flinging. */
        override suspend fun onPreFling(available: Velocity): Velocity {
            if (pull == 0f) return Velocity.Zero
            settle()
            return available
        }
    }
}

@Composable
fun rememberBounceScrollState(): BounceScrollState = remember { BounceScrollState() }

/**
 * Gives a scrollable a rubber-band bounce at both ends. Apply to the scrollable
 * itself; modifier order makes this the scrollable's nested-scroll parent.
 *
 * [clipToBounds] has to come before the layer: `graphicsLayer` does not clip its
 * own translated content, so without it a pulled list would draw outside its
 * bounds and over whatever sits above or below -- the pinned bar, in this app.
 */
fun Modifier.bounceScroll(state: BounceScrollState): Modifier =
    this.nestedScroll(state.connection)
        .clipToBounds()
        .graphicsLayer { translationY = state.pull }
