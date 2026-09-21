import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeCompiler)
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

android {
    namespace = "com.grocemaxxer.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.grocemaxxer.android"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        // Bump versionCode on every upload to Play; Play rejects a reused one.
        versionCode = 1
        versionName = "1.0.0"
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
