plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // To add Android, apply the `com.android.library` plugin above, add
    // `androidTarget()` here, and create shared/src/androidMain. Requires
    // the Android SDK + Google's Maven repo (see README.md).

    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
