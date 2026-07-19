package com.grocemaxxer.app

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * OAuth 2.0 *Web application* client ID from Google Cloud Console.
 * Sign-in is disabled until this is filled in -- see README,
 * "Google Sign-In setup", for the exact console steps.
 */
const val GOOGLE_WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"

actual suspend fun platformSignInWithGoogle(): UserAccount? {
    if (GOOGLE_WEB_CLIENT_ID.startsWith("YOUR_")) return null
    val activity = GrocemaxxerAndroidContext.currentActivity ?: return null

    val googleIdOption = GetGoogleIdOption.Builder()
        .setServerClientId(GOOGLE_WEB_CLIENT_ID)
        // Show all Google accounts on the device, not just previously used ones.
        .setFilterByAuthorizedAccounts(false)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val result = CredentialManager.create(activity).getCredential(activity, request)
        val credential = result.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            UserAccount(
                email = googleCredential.id,
                displayName = googleCredential.displayName ?: googleCredential.id,
            )
        } else {
            null
        }
    } catch (_: GetCredentialException) {
        null
    }
}

actual suspend fun platformSignOutGoogle() {
    try {
        CredentialManager.create(GrocemaxxerAndroidContext.appContext)
            .clearCredentialState(ClearCredentialStateRequest())
    } catch (_: Exception) {
        // Losing the platform credential-session state on logout is not fatal.
    }
}
