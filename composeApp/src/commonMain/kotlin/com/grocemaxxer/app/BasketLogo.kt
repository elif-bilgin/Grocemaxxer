package com.grocemaxxer.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import org.jetbrains.compose.resources.painterResource

import grocemaxxer.composeapp.generated.resources.Res
import grocemaxxer.composeapp.generated.resources.grocemaxxer_logo

@Composable
fun BasketLogo(
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(Res.drawable.grocemaxxer_logo),
        contentDescription = "grocemaxxer logo",
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit
    )
}