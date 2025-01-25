plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    androidTarget {

    }

    jvm{

    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":lib-ihttp-core"))
                api(libs.okhttp)
            }
        }
    }
}

android {
    namespace = "com.ustadmobile.ihttp.okhttp.android"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        jvmToolchain(21)
    }
}

