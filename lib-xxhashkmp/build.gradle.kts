
plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    androidTarget {

    }

    jvm {

    }

    js(IR) {
        useCommonJs()
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":lib-xxhashkmp-core"))
                implementation(kotlin("stdlib-common"))
                implementation(libs.coroutines)
            }
        }

        val jsMain by getting {

        }


        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
            }
        }


        val jvmMain by getting {
            dependencies {
                api(project(":lib-xxhashkmp-commonjvmimpl"))
            }
        }

        val jvmTest by getting {
            dependencies {

            }
        }

        val androidMain by getting {
            dependencies {
                api(project(":lib-xxhashkmp-commonjvmimpl"))
            }
        }

    }
}

android {
    compileSdk = 35
    namespace = "com.ustadmobile.xxhashkmp"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        minSdk = 21
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}
