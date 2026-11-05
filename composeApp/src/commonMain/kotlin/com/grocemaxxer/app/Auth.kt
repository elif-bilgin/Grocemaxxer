package com.grocemaxxer.app

/**
 * Provider-agnostic contract for email sign-in with a second factor.
 *
 * No backend is wired in yet. The screen and the session handling are built
 * against this interface so that choosing Supabase, Firebase or anything else
 * later is an implementation detail rather than a UI rewrite.
 */
interface AuthClient {

    /**
     * Whether a real backend is behind this client.
     *
     * When false the app skips the login gate entirely and behaves exactly as
     * it does today. That is deliberate: an app that cannot authenticate must
     * not present a door it can never open.
     */
    val isConfigured: Boolean

    suspend fun signUp(email: String, password: String): AuthOutcome

    suspend fun signIn(email: String, password: String): AuthOutcome

    /** Second step, after [signIn] or [signUp] returns [AuthOutcome.MfaRequired]. */
    suspend fun submitMfaCode(code: String): AuthOutcome
}

sealed interface AuthOutcome {
    /** Fully authenticated, including the second factor. */
    data class Success(val session: AuthSession) : AuthOutcome

    /** Credentials accepted; the app must now collect a one-time code. */
    data object MfaRequired : AuthOutcome

    /** Shown to the user verbatim, so [message] has to be human-readable. */
    data class Failure(val message: String) : AuthOutcome
}

/**
 * A signed-in session, persisted so the user is not asked again.
 *
 * [issuedForVersion] records the app's versionCode at sign-in. The session is
 * discarded when the app is updated, which is the logout policy asked for:
 * stay signed in indefinitely, except across an update.
 */
data class AuthSession(
    val email: String,
    val token: String,
    val issuedForVersion: Int,
)

/**
 * The only client that exists today. Reports itself unconfigured, so the app
 * runs exactly as it does now, and refuses every call rather than pretending
 * to have authenticated anyone.
 */
object UnconfiguredAuthClient : AuthClient {
    override val isConfigured: Boolean = false

    private fun refuse() = AuthOutcome.Failure(
        "Sign-in isn't set up yet in this build.",
    )

    override suspend fun signUp(email: String, password: String) = refuse()
    override suspend fun signIn(email: String, password: String) = refuse()
    override suspend fun submitMfaCode(code: String) = refuse()
}

/**
 * ⚠️ NOT AUTHENTICATION. Accepts any email, any password of six characters or
 * more, and the code 000000. It exists only so the login screen can be opened
 * and reviewed before a backend is chosen, and it stores nothing anywhere.
 *
 * Never point [activeAuthClient] at this in a build that reaches a user.
 */
object PreviewOnlyAuthClient : AuthClient {
    override val isConfigured: Boolean = true

    private var pendingEmail: String? = null

    private fun start(email: String, password: String): AuthOutcome = when {
        !email.contains('@') -> AuthOutcome.Failure("That doesn't look like an email address.")
        password.length < 6 -> AuthOutcome.Failure("Passwords are at least 6 characters.")
        else -> {
            pendingEmail = email
            AuthOutcome.MfaRequired
        }
    }

    override suspend fun signUp(email: String, password: String) = start(email, password)
    override suspend fun signIn(email: String, password: String) = start(email, password)

    override suspend fun submitMfaCode(code: String): AuthOutcome {
        val email = pendingEmail ?: return AuthOutcome.Failure("Start again from the top.")
        if (code != "000000") return AuthOutcome.Failure("That code isn't right.")
        pendingEmail = null
        return AuthOutcome.Success(
            AuthSession(email = email, token = "preview", issuedForVersion = appVersionCode()),
        )
    }
}

/**
 * The client the app uses.
 *
 * Swap to a real implementation once a provider is chosen. Swap to
 * [PreviewOnlyAuthClient] *locally only* to walk through the screen.
 */
val activeAuthClient: AuthClient = UnconfiguredAuthClient
