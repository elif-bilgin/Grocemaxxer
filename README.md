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
- A Room 3 database of pre-saved items (~250, seeded from
  `shared/.../PresetCatalog.kt`) powers live suggestions as you type in
  the add sheet plus a browse-by-aisle picker. The catalog is
  intentionally static -- typed items are not remembered, so a one-off
  typo never becomes a recurring suggestion.
- Text-based sharing: send the list through the share sheet as a
  readable message, paste a received one to see a diff against yours,
  and opt in before overwriting.

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

## Development notes

The business logic (categorization, store orderings, the share/merge codec,
date handling) lives in `shared` with no UI or platform dependencies, and is
covered by unit tests -- `./gradlew :shared:jvmTest` is the fastest way to
check a change. The app stores everything on-device and requires no network
access or account.
