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
        browser {

        }

    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(libs.coroutines)
                implementation(libs.ktor.client.core)
                api(libs.kotlinxio.core)
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
                implementation(libs.okhttp)
                implementation(project(":lib-ihttp-iostreams"))
            }
        }

        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation(project(":lib-test-common"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(project(":lib-ihttp-iostreams"))
            }
        }

    }
}

android {
    compileSdk = 34
    namespace = "com.ustadmobile.ihttp.core"

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