
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.atomicfu")
}

kotlin {
    androidTarget {

    }

    jvm {

    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(project(":lib-xxhashkmp-core"))
                implementation(libs.lz4.pure.java)
                implementation(libs.coroutines)
                implementation(libs.atomicfu)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
            }
        }


        val jvmMain by getting {
            dependencies {

            }
        }

        val jvmTest by getting {
            dependencies {

            }
        }

        val androidMain by getting {
            dependencies {

            }
        }

    }
}

android {
    compileSdk = 34
    namespace = "com.ustadmobile.xxhashkmp.commonjvmimpl"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = 21
        targetSdk = 34
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}
