# Email accounts: what is built, and what finishing it requires

Status: **UI and session handling only.** There is no auth backend, and the
login gate is inert — `UnconfiguredAuthClient.isConfigured` is false, so the
app skips the login screen and behaves exactly as it does without accounts.

This branch is deliberately not merged into `grocemaxxer`, which stays as the
build for the closed test.

## What exists

| Piece | File |
| --- | --- |
| Provider-agnostic contract | `composeApp/.../Auth.kt` — `AuthClient`, `AuthOutcome`, `AuthSession` |
| Login + MFA screen | `composeApp/.../LoginScreen.kt` |
| Session persistence | `composeApp/.../AuthRepository.kt` |
| Version-aware expiry | `composeApp/.../AppVersion.kt` + actuals |
| Gate wiring | `AppScreen.Login` in `App.kt` |

The screen is the start screen's layout — logo, wordmark, tagline — with
email and password in place of the list buttons, then a second step for the
6-digit code. Sign-in and sign-up share it, toggled by a text button.

**Sessions never expire except on update.** `AuthSession.issuedForVersion`
records the version code at sign-in; `AuthRepository.session` returns null
when it no longer matches `appVersionCode()`. That is the requested policy,
and it doubles as a way to force re-authentication from a release.

### Seeing the screen before a backend exists

`PreviewOnlyAuthClient` accepts any email, any password of 6+ characters and
the code `000000`. Point `activeAuthClient` at it locally to walk the flow.
It authenticates nothing and must never reach a user.

## What finishing it requires

Choosing a provider is the small part. These are the rest, and the first one
is a Play requirement rather than a nicety:

1. **Account deletion.** Play requires any app offering account creation to
   provide in-app account deletion *and* a publicly reachable web URL where
   deletion can be requested. Both, not either. Neither exists yet.
2. **Data safety declaration.** Currently "collects no user data". With
   accounts it becomes email address and user ID, collected and transmitted,
   with encryption in transit declared.
3. **`PRIVACY.md` rewrite.** It opens "Nothing. GroceMaxxer has no user
   accounts, no sign-in…" — false the moment this ships.
4. **Store listing rewrite.** "NO ACCOUNT, NO INTERNET, NO ADS" is a heading
   in the full description.
5. **`INTERNET` permission** in the manifest. Deliberately removed in
   `28cba36`.
6. **Offline behaviour.** The app's pitch includes working in shops with no
   signal. A saved session covers the normal case, but first sign-in needs
   network, and the screen needs to say so rather than just failing.
7. **R8 keep rules** for whichever SDK is added, plus a fresh release
   smoke-test.

## Note on what an account currently buys the user

Lists are stored on-device and nothing syncs. Until there is a server-side
list, an account gates the app without protecting or enabling anything, which
is the sort of thing closed-test feedback tends to object to. The feature
earns its place as groundwork for cloud sync or multi-device lists.
