rootProject.name = "Grocemaxxer"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":shared")
include(":composeApp")

// The Android app target lives in ./androidApp and is intentionally NOT
// included by default. See README.md ("Adding the Android target") --
// enabling it requires the Android SDK / Google's Maven repository, which
// is not reachable from every environment. Once available, add:
// include(":androidApp")
