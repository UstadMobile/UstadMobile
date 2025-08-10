
plugins {
    id("com.android.test")
    id("org.jetbrains.kotlin.android")
    id("androidx.baselineprofile")
}

android {
    namespace = "com.ustadmobile.app_android_baselineprofile"
    compileSdk = 35

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    defaultConfig {
        minSdk = 28
        targetSdk = 35

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app-android"

    @Suppress("UnstableApiUsage")
    testOptions {
        managedDevices {
            devices {
                localDevices {
                    create("pixel2api30") {
                        // Use device profiles you typically see in Android Studio.
                        device = "Pixel 2"
                        // Use only API levels 27 and higher.
                        apiLevel = 30
                        // To include Google services, use "google".
                        systemImageSource = "aosp"
                    }
                }
            }
        }
    }
}

// This is the configuration block for the Baseline Profile plugin.
// You can specify to run the generators on a managed devices or connected devices.
baselineProfile {
    useConnectedDevices = false
    managedDevices += "pixel2api30"
}

dependencies {
    implementation(libs.androidx.test.junit.ext)
    implementation(libs.androidx.test.espresso.core)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}