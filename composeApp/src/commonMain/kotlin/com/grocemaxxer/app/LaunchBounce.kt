package com.grocemaxxer.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/** How long the whole drop-and-settle takes. */
private const val DURATION_MS = 1150

/**
 * Whether this process has already played its launch entrance.
 *
 * A plain process-scoped flag is exactly the lifetime wanted: it is false on a
 * cold start and after the system kills the app, and it survives everything
 * that is not a cold start -- coming back from the background, and on Android
 * an activity recreated for a rotation or a theme change. So the entrance plays
 * on the first screen of a launch and never interrupts someone mid-shop.
 */
object LaunchEntrance {
    private var played = false

    /** Returns true to the first caller of a process, false to every other. */
    fun claim(): Boolean {
        if (played) return false
        played = true
        return true
    }
}

// Keyframes for the drop: dp above the resting position, against progress.
// It falls in, overshoots as it lands, and settles over two smaller rebounds.
private val DROP_DP = listOf(
    0f to -34f, 0.38f to 0f, 0.54f to -11f, 0.70f to 0f, 0.84f to -4f, 1f to 0f,
)

// Squash and stretch, which is what sells the weight: it narrows and lengthens
// while falling, spreads flat on impact, and recovers.
private val SCALE_X = listOf(
    0f to 0.96f, 0.38f to 1.08f, 0.54f to 0.97f, 0.70f to 1.04f, 0.84f to 0.99f, 1f to 1f,
)
private val SCALE_Y = listOf(
    0f to 1.06f, 0.38f to 0.90f, 0.54f to 1.04f, 0.70f to 0.95f, 0.84f to 1.01f, 1f to 1f,
)

/**
 * Reads a keyframe table at [t], smoothing within each segment.
 *
 * Interpolating a segment linearly would make the curve change direction with a
 * visible corner at every keyframe; smoothstep eases each segment in and out, so
 * the bounce reads as one continuous movement.
 */
internal fun sampleCurve(points: List<Pair<Float, Float>>, t: Float): Float {
    if (t <= points.first().first) return points.first().second
    if (t >= points.last().first) return points.last().second
    for (i in 0 until points.size - 1) {
        val (t0, v0) = points[i]
        val (t1, v1) = points[i + 1]
        if (t >= t0 && t <= t1) {
            if (t1 == t0) return v1
            val u = (t - t0) / (t1 - t0)
            return v0 + (v1 - v0) * (u * u * (3f - 2f * u))
        }
    }
    return points.last().second
}

/**
 * Drops this composable in from above and lets it bounce to rest.
 *
 * Pass [play] false and it renders at rest with no animation at all, so the same
 * modifier can stay on the element whether or not this launch earned an entrance.
 * [strength] scales the whole movement, for giving a second element a lighter
 * version of the same motion rather than an identical copy.
 */
@Composable
fun Modifier.launchBounce(
    play: Boolean,
    delayMillis: Int = 0,
    strength: Float = 1f,
): Modifier {
    val progress = remember { Animatable(if (play) 0f else 1f) }
    LaunchedEffect(play) {
        if (play && progress.value < 1f) {
            progress.animateTo(
                targetValue = 1f,
                // Linear, because the shaping lives in the keyframes above.
                animationSpec = tween(DURATION_MS, delayMillis, LinearEasing),
            )
        }
    }
    return this.graphicsLayer {
        val t = progress.value
        translationY = (sampleCurve(DROP_DP, t) * strength).dp.toPx()
        scaleX = 1f + (sampleCurve(SCALE_X, t) - 1f) * strength
        scaleY = 1f + (sampleCurve(SCALE_Y, t) - 1f) * strength
        // Hidden through its delay, then in quickly, so it is already visible
        // for the fall rather than arriving after it.
        alpha = if (t <= 0f) 0f else (t / 0.18f).coerceAtMost(1f)
    }
}
