
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

        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(libs.coroutines)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(npm("xxhashjs", libs.versions.xxhashjs.get()))
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.js)
                implementation("org.jetbrains.kotlin-wrappers:kotlin-js:${libs.versions.kotlin.wrappers.get()}")
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
    compileSdk = 35
    namespace = "com.ustadmobile.xxhashkmp.core"

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
