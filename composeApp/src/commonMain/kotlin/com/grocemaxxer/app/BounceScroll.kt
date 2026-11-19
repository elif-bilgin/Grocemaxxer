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

/** How far the list can be pulled past its end, in pixels. */
private const val MAX_PULL_PX = 180f

/** Fraction of an overscrolling drag that turns into pull at rest. */
private const val PULL_RESISTANCE = 0.4f

/**
 * A rubber-band bounce for the ends of a scrolling list.
 *
 * Compose Multiplatform 1.7 has no way to hand a custom [androidx.compose.foundation.OverscrollEffect]
 * to a `LazyColumn` -- neither `LazyColumn` nor `Modifier.scrollable` takes one at
 * this version, and `Modifier.overscroll` only *draws* an effect without ever
 * feeding it scroll deltas. So the bounce is built on nested scroll instead, which
 * is stable API here.
 *
 * Consuming the leftover delta in [NestedScrollConnection.onPostScroll] is what
 * suppresses the platform's own overscroll: the scroll machinery wraps the whole
 * pre-scroll/scroll/post-scroll chain in `OverscrollEffect.applyToScroll`, so an
 * effect only sees what parent connections did not take. Taking all of it starves
 * Android's stretch, leaving just this bounce rather than the two fighting.
 */
class BounceScrollState {
    /** Pixels the list is currently displaced by; positive is downward. */
    var pull by mutableFloatStateOf(0f)
        private set

    /**
     * Pull resists progressively, so the list slows to a stop near [MAX_PULL_PX]
     * instead of tracking the finger one-to-one off the end of the screen.
     */
    private fun damped(delta: Float): Float {
        val progress = (abs(pull) / MAX_PULL_PX).coerceIn(0f, 1f)
        return delta * (1f - progress) * PULL_RESISTANCE
    }

    private suspend fun settle() {
        if (pull == 0f) return
        // Cancelled if the user touches down again mid-spring, which leaves `pull`
        // wherever the animation got to -- exactly where the next drag should resume.
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
        /** Scrolling back toward the content pays the pull down before the list moves. */
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (pull == 0f || available.y == 0f || sign(available.y) == sign(pull)) {
                return Offset.Zero
            }
            val next = pull + available.y
            return if (sign(next) != sign(pull)) {
                // The pull is used up; hand the rest of the gesture to the list.
                val spent = -pull
                pull = 0f
                Offset(0f, spent)
            } else {
                pull = next
                Offset(0f, available.y)
            }
        }

        /** Whatever the list could not scroll is at its end, so it becomes pull. */
        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset {
            if (available.y == 0f) return Offset.Zero
            pull = (pull + damped(available.y)).coerceIn(-MAX_PULL_PX, MAX_PULL_PX)
            return Offset(0f, available.y)
        }

        /** Released while overscrolled: spring back instead of flinging. */
        override suspend fun onPreFling(available: Velocity): Velocity {
            if (pull == 0f) return Velocity.Zero
            settle()
            return available
        }

        /** A fling that ran into the end builds pull via onPostScroll; release it. */
        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
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
 * bounds and over whatever sits above or below -- the date bar, in this app.
 */
fun Modifier.bounceScroll(state: BounceScrollState): Modifier =
    this.nestedScroll(state.connection)
        .clipToBounds()
        .graphicsLayer { translationY = state.pull }
