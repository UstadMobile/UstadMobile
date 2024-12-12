plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.serialization)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("SystemDb") {
            srcDirs("src/main/sqldelight")
            packageName.set("com.ustadmobile.systemdb.sqlite")
        }
    }
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
                implementation(project(":lib-systemdb-core"))
                implementation(project(":lib-xxhashkmp"))
                implementation(libs.coroutines)
                implementation(libs.ktor.client.core)
                implementation(libs.kotlinx.serialization)
                implementation(libs.coroutines)
                implementation(libs.ktor.client.core)
                implementation(libs.sqldelight.coroutine.extensions)
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
                implementation(libs.ktor.server.core)
                implementation(libs.kodein.di.framework.ktor.server.jvm)
                api(libs.sqldelight.sqlite.driver.jvm)
            }
        }

        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {

            }
        }

        val androidMain by getting {
            dependencies {
                api(libs.sqldelight.sqlite.driver.android)
            }
        }

    }
}

android {
    compileSdk = 34
    namespace = "com.ustadmobile.systemdb.sqldelight"

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
