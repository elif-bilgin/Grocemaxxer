package com.grocemaxxer.app

import androidx.compose.runtime.Composable

/**
 * iOS status bar style is driven by the hosting view controller rather than
 * from Compose; nothing to do here.
 */
@Composable
actual fun SystemBarAppearance(darkMode: Boolean) = Unit
