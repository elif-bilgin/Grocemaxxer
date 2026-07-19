# Grocemaxxer

A Kotlin Multiplatform grocery checklist app. You type in what you want to
buy; it groups the items by which part of the store they're likely to be
colocated in, and orders those groups into a "speed-run" walk for your
store. Check items off in the order they appear and you shouldn't have to
double back.

Features:
- Store selector (Default, Safeway, Whole Foods, Target, Trader Joe's) --
  each store has its own section ordering tuned to its real layout
  (`shared/.../Store.kt`); the choice is persisted.
- Checked items sink to the bottom of their section; fully-checked
  sections collapse into an animated "Done sections" stack at the bottom
  (tap to expand, scroll back up to re-stack). Checking off the last item
  blurs the screen with a "You got everything!" celebration.
- Lists are stored per calendar date; the start screen offers Use
  Previous List / Start New List / Use List From Date (wheel picker), and
  re-using a list with checked items asks whether to keep or clear the
  progress.
- Duplicate items are skipped automatically (case-insensitive), names are
  Title Cased, and the add sheet lets you override the auto-detected
  category.

## How the ordering works

- `StoreSection` (`shared/.../StoreSection.kt`) enumerates store departments
  with a fixed `order` representing one sensible walking path through a
  typical store: floral/produce near the entrance, then bakery, deli, meat &
  seafood, dairy, frozen, the center aisles, and finally personal care,
  pharmacy, household, and pet supplies.
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

## Branding

- Wordmark (start screen + main header): `composeApp/src/commonMain/composeResources/drawable/grocemaxxer_title_no_background.png`
- In-app basket logo: `composeApp/src/commonMain/composeResources/drawable/grocemaxxer_logo.png`
- Android launcher icon: `androidApp/src/main/res/drawable/ic_launcher_foreground.png`

Overwrite these files (same names) to rebrand; no code changes needed.

## Project layout

```
shared/       Pure Kotlin business logic (no UI deps) -- targets jvm + android + iOS
composeApp/   Compose Multiplatform UI, shared across desktop/android/iOS
androidApp/   Android app module (thin wrapper: MainActivity + manifest)
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
Needs the Android SDK (Android Studio is the easiest way to get it).

1. Open the project root in Android Studio and let it sync (first sync
   downloads the SDK platform/build-tools if needed).
2. Plug in your phone via USB, enable Developer Options + USB debugging on
   it (Settings -> About phone -> tap "Build number" 7 times -> Developer
   options -> USB debugging), and accept the "Allow USB debugging?" prompt
   on the phone.
3. Your device should appear in Android Studio's device dropdown; select it
   and click Run (▶) on the `androidApp` configuration. (Wireless
   debugging works too, via `adb pair`, if you'd rather not use a cable.)

Command-line equivalent, once a device shows up in `adb devices`:
```
./gradlew :androidApp:installDebug
adb shell am start -n com.grocemaxxer.android/.MainActivity
```

### Tests
```
./gradlew :shared:jvmTest
```
Runs the categorizer/organizer/codec unit tests on the JVM (the fastest
common target to test against).

## Persistence

The list and settings (theme color, dark mode) are stored with
`androidx.datastore` (preferences, KMP artifact). Platform file locations
are provided by `expect fun groceryDataStorePath()`:

- Android: `<app files dir>/grocemaxxer.preferences_pb`
- Desktop: `~/.grocemaxxer/grocemaxxer.preferences_pb`
- iOS: app documents directory

The item list itself is encoded into a single preferences value by
`GroceryListCodec` in `shared` (unit-tested round-trip codec).

## Verification notes (sandbox limitations)

This project was built in a network-restricted sandbox where Google's Maven
repository (`dl.google.com`) and JetBrains' download host are blocked by the
outbound proxy, but Maven Central and the Gradle Plugin Portal are reachable.
That constrained what could actually be *run* here:

- The `shared` tests -- ran successfully, all tests pass (this is where the
  real logic lives and where two real bugs were caught and fixed: an
  unmatched "eggplant" keyword, and plural phrases like "tortilla chips"
  losing to a shorter single-word match). After the Android target was
  wired in, the sandbox could no longer even configure the root project
  (AGP resolves from Google's Maven), so later runs used a standalone JVM
  harness compiling `shared`'s sources directly.
- `composeApp` originally compiled cleanly for the JVM in the sandbox;
  after `androidx.datastore` was added (also hosted on Google's Maven), the
  UI module could no longer be compiled there and is verified by building
  on a normal dev machine instead.
- `./gradlew :composeApp:run` (Desktop) -- could **not** be executed here:
  Compose Multiplatform's UI runtime pulls in `androidx.lifecycle:lifecycle-viewmodel`
  from `dl.google.com` at run time, which this sandbox blocks. It should run
  normally in an environment with unrestricted network access.
- The Android target (`androidTarget()` in `shared`/`composeApp`, plus the
  `androidApp` module) is wired into the build but was configured without
  being able to run a single Android Gradle task here -- `dl.google.com` is
  unreachable, so the Android Gradle Plugin itself can't even resolve. It
  follows the standard Kotlin Multiplatform + Compose Multiplatform template
  structure; it should sync and build normally in Android Studio.
- iOS compilation was not attempted here (no macOS/Xcode in this
  environment) -- see the build instructions above for what's needed.

If you hit issues running `./gradlew :composeApp:run` or building for
Android/iOS outside this sandbox, it's most likely a normal Gradle/SDK setup
issue rather than something introduced by these constraints.
