package com.grocemaxxer.app

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS has no system back button; edge-swipe navigation would come from
    // a navigation controller, which this single-screen app doesn't use.
}
