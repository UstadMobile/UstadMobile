plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("kotlin-parcelize")
    alias(libs.plugins.jetbrains.compose)
}

//Roughly as per compose multiplatform template
// see https://github.com/JetBrains/compose-multiplatform-template/blob/main/shared/build.gradle.kts

kotlin {
    androidTarget {

    }

    jvm("desktop")

    sourceSets {
        val commonMain by getting {

            dependencies {
                api(project(":core"))
                api(project(":lib-database"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                implementation(libs.kodein.di)
                implementation(libs.moko.resources)
                implementation(libs.moko.resources.compose)

                implementation(libs.kotlinx.datetime)

                api(libs.paging.multiplatform.common)
                api(libs.paging.multiplatform.compose)

                implementation(libs.reorderable.compose)
            }
        }

        val androidMain by getting {
            dependencies {
                api(libs.activity.compose)
                api(libs.androidx.appcompat)
                api(libs.androidx.core.ktx)
                api(libs.androidx.paging.runtime)
                api(libs.androidx.paging.compose)
                implementation(compose.material)

                val version_android_material = "1.9.0"
                implementation("com.google.android.material:material:$version_android_material")

                //HTML editor
                val version_android_aztec = "v1.6.3"
                implementation("org.wordpress:aztec:$version_android_aztec")
            }
        }

        val desktopMain by getting {
            dependencies {
                api(compose.desktop.common)
                implementation(compose.desktop.currentOs)
                implementation(libs.desktop.richeditor.compose)
            }
        }
    }

}

android {
    compileSdk = 33
    namespace = "com.ustadmobile.libuicompose"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = 21
        targetSdk = 33
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}