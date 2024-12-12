
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.serialization)
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
                implementation(kotlin("stdlib-common"))
                implementation(libs.coroutines)
                implementation(libs.ktor.client.core)
                implementation(libs.kotlinx.serialization)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
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
            dependsOn(commonTest)
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
    namespace = "com.ustadmobile.systemdb"

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
