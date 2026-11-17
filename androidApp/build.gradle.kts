import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
    // On the classpath, applied below only when credentials exist.
    alias(libs.plugins.playPublisher) apply false
}

/**
 * Upload-key material for release builds. Kept out of the repo: either put a
 * `keystore.properties` at the project root (gitignored) with
 * `storeFile` / `storePassword` / `keyAlias` / `keyPassword`, or set the
 * matching `GROCEMAXXER_*` environment variables in CI. When neither is
 * present the release build type still assembles -- unsigned -- so debug
 * workflows and CI checks don't need the secret.
 */
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun signingValue(key: String, environmentVariable: String): String? =
    (keystoreProperties.getProperty(key) ?: System.getenv(environmentVariable))
        ?.takeIf { it.isNotBlank() }

val releaseStoreFile = signingValue("storeFile", "GROCEMAXXER_STORE_FILE")
    ?.let { rootProject.file(it) }
    ?.takeIf { it.exists() }
val releaseStorePassword = signingValue("storePassword", "GROCEMAXXER_STORE_PASSWORD")
val releaseKeyAlias = signingValue("keyAlias", "GROCEMAXXER_KEY_ALIAS")
val releaseKeyPassword = signingValue("keyPassword", "GROCEMAXXER_KEY_PASSWORD")
val hasReleaseSigning = releaseStoreFile != null &&
    releaseStorePassword != null &&
    releaseKeyAlias != null &&
    releaseKeyPassword != null

/**
 * Publishing to Play through the Google Play Developer API.
 *
 * Applied only when credentials are present, which keeps ordinary builds
 * completely unaffected by it. That matters here: Gradle Play Publisher is in
 * maintenance mode and its latest release predates this project's AGP, so its
 * compatibility is unverified. Applied unconditionally, an incompatibility
 * would break every build; applied this way, the worst case is that the
 * publish tasks do not work and `bundleRelease` carries on as before.
 *
 * Credentials come from either `play-service-account.json` at the repo root
 * (gitignored), or the ANDROID_PUBLISHER_CREDENTIALS environment variable,
 * which holds the JSON file's *contents* rather than a path.
 */
val playCredentialsFile = rootProject.file("play-service-account.json").takeIf { it.exists() }
val hasPlayCredentials = playCredentialsFile != null ||
    !System.getenv("ANDROID_PUBLISHER_CREDENTIALS").isNullOrBlank()

if (hasPlayCredentials) {
    apply(plugin = "com.github.triplet.play")
    configure<com.github.triplet.gradle.play.PlayPublisherExtension> {
        playCredentialsFile?.let { serviceAccountCredentials.set(it) }
        // Override per invocation with -Pplay.track=<track name>.
        track.set(providers.gradleProperty("play.track").orElse("internal"))
        defaultToAppBundles.set(true)
    }
}

android {
    namespace = "com.grocemaxxer.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        // The identity registered in Play Console. Permanent once the
        // first bundle is uploaded. Deliberately not the same as the
        // namespace below, which only governs where R and BuildConfig
        // are generated and what ".MainActivity" resolves against.
        applicationId = "com.groovy.grocemaxxer"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        // Bump versionCode on every upload to Play; Play rejects a reused one.
        versionCode = 2
        versionName = "1.1.0"
    }

    if (hasReleaseSigning) {
        signingConfigs {
            create("release") {
                storeFile = releaseStoreFile
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
}

/** Fails loudly rather than shipping an unsigned bundle to Play by accident. */
tasks.register("checkReleaseSigning") {
    group = "verification"
    description = "Verifies that upload-key material is configured for release builds."
    doLast {
        check(hasReleaseSigning) {
            "No release signing configured. Create keystore.properties (see " +
                "docs/play-store-release.md) or set the GROCEMAXXER_* environment variables."
        }
    }
}
