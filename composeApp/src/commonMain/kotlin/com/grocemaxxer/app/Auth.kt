package com.grocemaxxer.app

/** A signed-in Google account, as much of it as the app needs. */
data class UserAccount(
    val email: String,
    val displayName: String,
)

/**
 * Launches the platform's Google sign-in UI and returns the account, or
 * null if the user cancelled, sign-in failed, or the platform doesn't
 * support it (desktop/iOS are currently unsupported). Real implementation
 * on Android via Credential Manager; requires GOOGLE_WEB_CLIENT_ID to be
 * configured (see README, "Google Sign-In setup").
 */
expect suspend fun platformSignInWithGoogle(): UserAccount?

/** Clears any platform-side credential session state on logout. */
expect suspend fun platformSignOutGoogle()
