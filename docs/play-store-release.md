# Releasing GroceMaxxer on Google Play

Everything needed to get a build from this repo into a Play Console release,
plus the answers to the Console questionnaires. GroceMaxxer ships free with no
ads, no in-app purchases, no accounts and no network access, which makes most
of these forms short.

## 1. Create the upload key (once)

Play uses Play App Signing: you sign the bundle with an *upload* key, Google
re-signs it with the app signing key. Generate the upload key once and keep it
safe — losing it means asking Google to reset it.

```
keytool -genkeypair -v \
  -keystore grocemaxxer-upload.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias grocemaxxer
```

Store the `.jks` somewhere backed up and **outside this repo**, then create
`keystore.properties` at the repo root (already gitignored):

```properties
storeFile=../grocemaxxer-upload.jks
storePassword=…
keyAlias=grocemaxxer
keyPassword=…
```

`storeFile` is resolved relative to the repo root. In CI, set
`GROCEMAXXER_STORE_FILE`, `GROCEMAXXER_STORE_PASSWORD`, `GROCEMAXXER_KEY_ALIAS`
and `GROCEMAXXER_KEY_PASSWORD` instead — the build reads either source.

When neither is configured the release build still assembles, unsigned, so
ordinary builds don't need the secret. To assert that signing *is* configured
before an upload, run `./gradlew :androidApp:checkReleaseSigning`.

## 2. Build the bundle

```
./gradlew :androidApp:bundleRelease
```

Output: `androidApp/build/outputs/bundle/release/androidApp-release.aab`.

### Smoke-test the minified build before uploading

The release build runs R8 (`isMinifyEnabled = true`) with keep rules in
`androidApp/proguard-rules.pro`. Those rules cover the bundled SQLite driver's
JNI entry points, Room's generated classes, DataStore's embedded protobuf and
the enum names the app persists — but R8 problems only ever show up at
runtime, so install a release build on a real device and exercise the app
before the first upload, and again after any dependency bump:

```
./gradlew :androidApp:installRelease
```

Then check, in a release build specifically:

- the app launches (Room and DataStore both initialise on first frame);
- adding an item shows catalogue suggestions and the browse-by-aisle picker
  (Room queries work);
- the list survives force-quitting and reopening (DataStore round-trip);
- the store selector, accent colour and dark-mode switch persist (enum names
  survived obfuscation);
- share produces a message and pasting it back imports cleanly.

If something breaks only in release, it is a missing keep rule — add it to
`proguard-rules.pro` rather than turning minification off.

`./gradlew :androidApp:installRelease` refuses to install over a debug build
of the same application ID (different signature). Run
`adb uninstall com.grocemaxxer.android` first.

### Verify 16 KB page-size compatibility

Since 1 November 2025 Play requires apps with native code that target API 35
or above to support 16 KB memory pages on 64-bit devices. GroceMaxxer has no
native code of its own, but `androidx.sqlite:sqlite-bundled` ships `.so`
files, so the requirement applies.

Both halves of it should already hold: AGP 8.5.1 and above align uncompressed
shared libraries at packaging time (this project is on 8.10.1), and the
AndroidX SQLite artifacts have been built 16 KB-aligned since 2.5.0. Confirm
it rather than assume it, once, before the first upload — a failure here is
rejected at upload time:

```
./gradlew :androidApp:assembleRelease
unzip -o androidApp/build/outputs/apk/release/androidApp-release.apk -d /tmp/gm-apk
find /tmp/gm-apk/lib -name '*.so' -print -exec \
  "$ANDROID_HOME"/ndk/*/toolchains/llvm/prebuilt/*/bin/llvm-readelf -l {} \; \
  | grep -E '\.so$|LOAD'
```

Every `LOAD` segment must show an alignment of `0x4000` (16 KB) or larger;
`0x1000` (4 KB) is the failing case. Android Studio's APK Analyzer reports the
same thing with a warning banner if you would rather not run the command.

If a library ever does fail this check, the fix is to upgrade that dependency
to a 16 KB-aligned release — never to lower `targetSdk`.

## 3. Play Console setup

**App details**

| Field | Value |
| --- | --- |
| App name | GroceMaxxer |
| Default language | English (United States) |
| App or game | App |
| Free or paid | Free (cannot be changed to paid later) |
| Category | Food & Drink (alternative: Shopping) |
| Contact email | _required; also fill in the placeholder in `PRIVACY.md`_ |
| Privacy policy URL | see below |
| Package name | `com.grocemaxxer.android` |

**Privacy policy URL.** `PRIVACY.md` in this repo is the policy text. Play
needs it at a public URL. The quickest route is GitHub Pages: enable Pages for
this repository (Settings → Pages → deploy from the default branch), which
serves it at `https://<user>.github.io/grocemaxxer/PRIVACY`. Fill in the
contact email placeholder before publishing.

**Data safety form**

- Does your app collect or share any of the required user data types? **No**
- Is all of the user data collected by your app encrypted in transit?
  *(not asked once you answer "no" above)*
- Do you provide a way for users to request that their data is deleted?
  **No data is collected** — the form stops here.

The justification, if asked: all data stays in app-private storage on the
device, the app has no internet permission, and no SDKs are bundled.

**Content rating questionnaire** (IARC) — category "Utility, Productivity,
Communication or Other". Every content question is **No**: no violence, no
sexuality, no profanity, no controlled substances, no gambling, no user
interaction, no user-generated content sharing within the app, no location
sharing, no personal information collection, no digital purchases. Expected
result: rated for everyone (ESRB Everyone / PEGI 3 / USK 0).

**Ads declaration** — the app contains no ads.

**Target audience and content** — not designed for children; select the 18+
(or 13+) age bands as appropriate. Because no data is collected, the Families
policy requirements do not apply.

**Government apps / financial features / health** — all No.

**App access** — "All functionality is available without special access"; there
is no login of any kind.

## 4. Store listing assets

Text (write once, paste into the Console):

- **App name** (30 chars max): `GroceMaxxer`
- **Short description** (80 chars max):
  `Your grocery run, optimized — items sorted into one walk through your store.`
  (76 characters)
- **Full description** (4000 chars max): see `docs/store-listing.md`.

Graphics:

| Asset | Spec | Status |
| --- | --- | --- |
| App icon | 512 × 512 PNG, 32-bit, no transparency | **ready** — `docs/store-assets/play-icon-512.png` |
| Feature graphic | 1024 × 500 PNG or JPEG, no transparency | **ready** — `docs/store-assets/play-feature-graphic-1024x500.png` |
| Phone screenshots | 2–8 images, 16:9 or 9:16, each side 320–3840 px | **you have to take these on a device** |

The icon is the launcher icon as the square mask shows it: the central 72dp of
the adaptive foreground, flattened onto the `#FDF3E7` icon background (Play
rejects transparency). The feature graphic is the wordmark on the same cream
background — replace it with something richer whenever you like; the spec, not
the design, is what blocks submission.

Both were generated by the snippet in `docs/store-assets/README.md`, so they
can be regenerated if the branding assets change.

Screenshots worth capturing, in this order — they tell the story of the app:

1. the welcome screen,
2. a populated list showing several sections in store order,
3. the add sheet with catalogue suggestions appearing,
4. the browse-by-aisle picker,
5. the "You got everything!" celebration,
6. the import screen showing a diff.

Tablet screenshots are optional but improve the listing's device coverage.

## 5. Release track

First upload goes to **internal testing** — it reviews in hours rather than
days and lets you check the Play-delivered build on a real device. Promote to
production once the smoke-test list above passes on the downloaded build.

New accounts publishing their first app may be asked to run a closed test
before production is unlocked; the Console states the current requirement at
the time of submission.

## 6. Every subsequent upload

- Bump `versionCode` (and usually `versionName`) in
  `androidApp/build.gradle.kts`. Play rejects a reused `versionCode`.
- Re-run the R8 smoke test if any dependency changed.
- Keep `targetSdk` current: Play requires new apps and updates to target
  API 36 as of 31 August 2026, and raises the bar each August.

## Reference: what makes the build Play-ready

| Requirement | Where it lives |
| --- | --- |
| `targetSdk` / `compileSdk` 36 | `gradle/libs.versions.toml` |
| Edge-to-edge (enforced from API 35) | `MainActivity.enableEdgeToEdge()`, insets applied in `App()` |
| System bar icon contrast | `SystemBarAppearance` expect/actual |
| No white flash on launch | `res/values/themes.xml` + `values-night` |
| R8 / resource shrinking | `androidApp/build.gradle.kts`, `proguard-rules.pro` |
| Upload signing | `keystore.properties` or `GROCEMAXXER_*` env vars |
| Backup and device-transfer rules | `res/xml/backup_rules.xml`, `res/xml/data_extraction_rules.xml` |
| Themed (monochrome) launcher icon | `res/mipmap-anydpi-v26/ic_launcher.xml` |
| Database upgrades never crash | `fallbackToDestructiveMigration` in `CatalogDatabase.kt` |
| 16 KB page-size support | AGP 8.10.1 packaging + `sqlite-bundled` 2.7.0 (verify once, above) |
| Privacy policy | `PRIVACY.md` |
