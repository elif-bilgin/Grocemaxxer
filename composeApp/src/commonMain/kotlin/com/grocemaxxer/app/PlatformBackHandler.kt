package com.grocemaxxer.app

import androidx.compose.runtime.Composable

/**
 * Intercepts the system back gesture while [enabled]. Real implementation
 * on Android; no-op on desktop and iOS (no system back gesture to catch).
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
