package com.grocemaxxer.app

import androidx.compose.runtime.Composable

/** Desktop windows have no system bars to tint. */
@Composable
actual fun SystemBarAppearance(darkMode: Boolean) = Unit
