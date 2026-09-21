package com.grocemaxxer.app

import androidx.compose.runtime.Composable

/**
 * Keeps the system bar icons readable against the app background.
 *
 * The app has its own dark-mode switch that may disagree with the system
 * setting, so the bar appearance follows [darkMode] rather than the platform
 * night mode. No-op on platforms without controllable system bars.
 */
@Composable
expect fun SystemBarAppearance(darkMode: Boolean)
