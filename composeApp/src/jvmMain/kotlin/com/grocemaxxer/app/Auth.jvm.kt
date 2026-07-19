package com.grocemaxxer.app

actual suspend fun platformSignInWithGoogle(): UserAccount? = null

actual suspend fun platformSignOutGoogle() {
    // No credential session to clear on desktop.
}
