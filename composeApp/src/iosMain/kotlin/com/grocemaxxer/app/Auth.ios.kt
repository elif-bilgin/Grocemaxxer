package com.grocemaxxer.app

actual suspend fun platformSignInWithGoogle(): UserAccount? = null

actual suspend fun platformSignOutGoogle() {
    // Google Sign-In for iOS would go through the GoogleSignIn SDK; not wired up yet.
}
