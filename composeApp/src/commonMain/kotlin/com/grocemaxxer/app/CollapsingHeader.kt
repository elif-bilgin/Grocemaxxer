package com.grocemaxxer.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.layout
import kotlin.math.roundToInt

/**
 * Drives a header that scrolls away with the list beneath it while everything
 * between it and the list stays put.
 *
 * The screen is tall on content and short on room: the wordmark and date are
 * worth a glance when you arrive and worth nothing once you are shopping, while
 * the store selector, the search field and the collected count have to stay
 * reachable however far down the list you are. So the header is given up for
 * space and the bar below it is pinned.
 *
 * Collapsing on pre-scroll and expanding on post-scroll is what makes it feel
 * attached to the list: scrolling down spends the header before the list moves
 * at all, and scrolling back up returns it only once the list has nothing left
 * to give, which is exactly when the list is back at its top.
 */
class CollapsingHeaderState {
    /** How far the header is currently shifted up: `-height`..`0`. */
    var offset by mutableFloatStateOf(0f)
        private set

    /**
     * The header's natural height, published by the modifier at measure time.
     * Deliberately a plain field: writing a snapshot state during measurement
     * would invalidate the layout that just wrote it.
     */
    private var height = 0f

    internal fun publishHeight(value: Float) {
        if (height == value) return
        height = value
        if (offset < -value) offset = -value
    }

    private fun consume(delta: Float): Offset {
        val before = offset
        offset = (offset + delta).coerceIn(-height, 0f)
        return Offset(0f, offset - before)
    }

    val connection = object : NestedScrollConnection {
        /** Scrolling down collapses the header before the list scrolls at all. */
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
            if (available.y >= 0f || height <= 0f || offset <= -height) Offset.Zero
            else consume(available.y)

        /** Scrolling up past the top of the list brings the header back. */
        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource,
        ): Offset =
            if (available.y <= 0f || height <= 0f || offset >= 0f) Offset.Zero
            else consume(available.y)
    }
}

@Composable
fun rememberCollapsingHeaderState(): CollapsingHeaderState = remember { CollapsingHeaderState() }

/**
 * Makes this composable the collapsing header for [state].
 *
 * It is measured at its natural height every pass and then *reports* a smaller
 * one, so the siblings below it move up into the space as it goes, rather than
 * it sliding out from under them and leaving a hole. Placing the content at the
 * negative offset inside that shortened box is what slides it up; [clipToBounds]
 * keeps the part now above the box from drawing over the status bar.
 */
fun Modifier.collapsingHeader(state: CollapsingHeaderState): Modifier =
    this.clipToBounds().layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        state.publishHeight(placeable.height.toFloat())
        val shift = state.offset.roundToInt()
        val height = (placeable.height + shift).coerceIn(0, placeable.height)
        layout(placeable.width, height) { placeable.place(0, shift) }
    }
