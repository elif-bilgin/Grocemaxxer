# Grocemaxxer

A Kotlin Multiplatform grocery checklist app. You type in what you want to
buy; it groups the items by which part of the store they're likely to be
colocated in, and orders those groups to match a low-backtracking walk
through a typical supermarket (produce/bakery near the entrance, perimeter
departments, frozen, center aisles, then health/household). Check items off
in the order they appear and you shouldn't have to double back.

## How the ordering works

- `StoreSection` (`shared/.../StoreSection.kt`) enumerates store departments
  with a fixed `order` representing one sensible walking path through a
  typical store.
- `ItemCategorizer` (`shared/.../ItemCategorizer.kt`) guesses an item's
  section from a static keyword table (`SectionKeywords.kt`), matching whole
  words/phrases (with basic plural handling) rather than raw substrings --
  e.g. "eggplant" won't be mistaken for "egg".
- `GroceryListOrganizer` groups items by section and sorts the groups by
  `StoreSection.order`, so the resulting checklist visits each department
  once, in path order.

This is a heuristic, not real per-store aisle data or a full traveling-
salesman solve over item pairs -- extend `SectionKeywords.kt` with more
keywords/sections, or swap `ItemCategorizer` for a real store's planogram
data, to make it more accurate for a specific store.

## Project layout

```
shared/       Pure Kotlin business logic (no UI deps) -- targets jvm + iOS
composeApp/   Compose Multiplatform UI, shared across desktop/iOS/(Android)
androidApp/   Android app module (not wired into the build by default, see below)
iosApp/       Swift entry point (needs an Xcode project wrapping it, see below)
```

## Building & running

### Desktop (Linux/macOS/Windows)
```
./gradlew :composeApp:run
```

### iOS
Needs Xcode/macOS. Create a new Xcode "App" project (SwiftUI, no storyboard)
named `iosApp` in `iosApp/`, replace its generated `ContentView.swift` /
`<Name>App.swift` with the ones already in `iosApp/iosApp/`, add a "Run
Script" build phase that runs `./gradlew :composeApp:embedAndSignAppleFrameworkForXcode`,
and link the `ComposeApp` framework it produces. (This is the standard
Compose Multiplatform iOS integration -- see
https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-ios.html)

### Android
The Android module is staged in `androidApp/` but **not** included in
`settings.gradle.kts` by default, and `shared`/`composeApp` don't declare an
Android target yet. To enable it (needs Android Studio / the Android SDK):

1. In `settings.gradle.kts`, uncomment `include(":androidApp")`.
2. In `shared/build.gradle.kts` and `composeApp/build.gradle.kts`, apply
   `id("com.android.library")` and add `androidTarget()` next to `jvm()`.
3. Add `shared/src/androidMain` / `composeApp/src/androidMain` source sets
   if you need Android-specific code (none is required to start).
4. Sync in Android Studio, run the `androidApp` configuration.

### Tests
```
./gradlew :shared:jvmTest
```
Runs the categorizer/organizer unit tests on the JVM (the fastest common
target to test against).

## Verification notes (sandbox limitations)

This project was built in a network-restricted sandbox where Google's Maven
repository (`dl.google.com`) and JetBrains' download host are blocked by the
outbound proxy, but Maven Central and the Gradle Plugin Portal are reachable.
That constrained what could actually be *run* here:

- `./gradlew :shared:jvmTest` -- ran successfully, all tests pass (this is
  where the real logic lives and where two real bugs were caught and fixed:
  an unmatched "eggplant" keyword, and plural phrases like "tortilla chips"
  losing to a shorter single-word match).
- `./gradlew :composeApp:compileKotlinJvm` -- compiles cleanly, confirming
  the UI code is valid against the Compose Multiplatform API.
- `./gradlew :composeApp:run` (Desktop) -- could **not** be executed here:
  Compose Multiplatform's UI runtime pulls in `androidx.lifecycle:lifecycle-viewmodel`
  from `dl.google.com` at run time, which this sandbox blocks. It should run
  normally in an environment with unrestricted network access.
- Android and iOS compilation were not attempted here (no Android SDK, no
  macOS/Xcode in this environment) -- see the build instructions above for
  what's needed to build them elsewhere.

If you hit issues running `./gradlew :composeApp:run` or building for
Android/iOS outside this sandbox, it's most likely a normal Gradle/SDK setup
issue rather than something introduced by these constraints.
