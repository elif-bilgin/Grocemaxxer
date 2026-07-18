package com.grocemaxxer.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The same cute pastel grocery basket as the Android launcher icon
 * (androidApp/.../ic_launcher_foreground.xml), rebuilt as a Compose
 * ImageVector so every platform's in-app logo matches the app icon.
 */
private val basketVector: ImageVector by lazy {
    ImageVector.Builder(
        name = "BasketLogo",
        defaultWidth = 108.dp,
        defaultHeight = 108.dp,
        viewportWidth = 108f,
        viewportHeight = 108f,
    ).apply {
        // Handle
        addPath(
            pathData = addPathNodes("M38,52 A16,16 0 0 1 70,52"),
            stroke = SolidColor(Color(0xFFC77B4F)),
            strokeLineWidth = 5f,
            strokeLineCap = StrokeCap.Round,
        )
        // Produce peeking over the rim
        addPath(
            pathData = addPathNodes("M44,46 m-7,0 a7,7 0 1 0 14,0 a7,7 0 1 0 -14,0"),
            fill = SolidColor(Color(0xFFA5CDA0)),
        )
        addPath(
            pathData = addPathNodes("M56,42 m-8,0 a8,8 0 1 0 16,0 a8,8 0 1 0 -16,0"),
            fill = SolidColor(Color(0xFFF19999)),
        )
        addPath(
            pathData = addPathNodes("M67,46 m-6.5,0 a6.5,6.5 0 1 0 13,0 a6.5,6.5 0 1 0 -13,0"),
            fill = SolidColor(Color(0xFFF6C982)),
        )
        // Basket body
        addPath(
            pathData = addPathNodes("M34,55 L74,55 L70.5,81 Q70,86 65,86 L43,86 Q38,86 37.5,81 Z"),
            fill = SolidColor(Color(0xFFF2B28C)),
        )
        // Weave detail
        addPath(
            pathData = addPathNodes("M45,57 L47.5,84"),
            stroke = SolidColor(Color(0xFFE8996B)),
            strokeLineWidth = 2.5f,
            strokeLineCap = StrokeCap.Round,
        )
        addPath(
            pathData = addPathNodes("M54,57 L54,84"),
            stroke = SolidColor(Color(0xFFE8996B)),
            strokeLineWidth = 2.5f,
            strokeLineCap = StrokeCap.Round,
        )
        addPath(
            pathData = addPathNodes("M63,57 L60.5,84"),
            stroke = SolidColor(Color(0xFFE8996B)),
            strokeLineWidth = 2.5f,
            strokeLineCap = StrokeCap.Round,
        )
        // Rim
        addPath(
            pathData = addPathNodes(
                "M31,50 L77,50 A3.5,3.5 0 0 1 80.5,53.5 A3.5,3.5 0 0 1 77,57 " +
                    "L31,57 A3.5,3.5 0 0 1 27.5,53.5 A3.5,3.5 0 0 1 31,50 Z",
            ),
            fill = SolidColor(Color(0xFFE8996B)),
        )
    }.build()
}

@Composable
fun BasketLogo(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .background(Color(0xFFFDF3E7), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            imageVector = basketVector,
            contentDescription = "grocemaxxer basket logo",
            modifier = Modifier.fillMaxSize(),
        )
    }
}
