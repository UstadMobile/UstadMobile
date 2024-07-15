plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("kotlin-parcelize")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.moko.resources.plugin)
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
                api(compose.runtime)
                api(compose.foundation)
                api(compose.animation)
                api(compose.material)
                api(compose.material3)
                api(compose.materialIconsExtended)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                api(compose.components.resources)
                api(libs.kodein.di)
                api(libs.kodein.di.compose)
                api(libs.moko.resources)
                api(libs.moko.resources.compose)
                api(libs.paging.multiplatform.common)
                api(libs.paging.multiplatform.compose)
                implementation(libs.chiptextfield)
                implementation(libs.kotlinx.datetime)
                implementation(libs.reorderable.compose)
                api(libs.door.runtime)
                api(libs.precompose)
                api(libs.precompose.viewmodel)
                implementation(libs.napier)
            }
        }

        val androidMain by getting {
            dependsOn(commonMain)

            dependencies {
                api(libs.activity.compose)
                api(libs.androidx.appcompat)
                api(libs.androidx.core.ktx)
                api(libs.androidx.paging.runtime)
                api(libs.androidx.paging.compose)
                api(libs.androidx.navigation.compose)
                api(libs.androidx.lifecycle.viewmodel.compose)
                api(libs.htmltext.android)
                api(libs.activity.compose)

                implementation(libs.volley)
                implementation(libs.aztec)

                implementation(libs.android.material)
                implementation(libs.libphonenumber.android)
                implementation(libs.autolink)
                implementation(libs.coil)
                implementation(libs.coil.compose)
                implementation(libs.compose.video)
                implementation(libs.androidx.media3.exoplayer)
                implementation(libs.androidx.media3.datasource.okhttp)
                implementation(libs.androidx.media3.ui)
                implementation(libs.pdfviewer)
                implementation(libs.nestedscroll.webview)
                implementation(libs.androidx.webkit)
                implementation(libs.androidx.constraintlayout)
                implementation(libs.androidx.media3.decoder.av1)
                implementation(libs.accompanist.drawablepainter)
                implementation(libs.zoomable)
            }
        }

        val desktopMain by getting {
            dependsOn(commonMain)

            dependencies {
                api(compose.desktop.common)
                implementation(compose.desktop.currentOs)

                implementation(libs.richeditor.multiplatform.compose)
                implementation(libs.libphonenumber.google)
                implementation(libs.autolink)
                implementation(libs.mpfilepicker)
                implementation(libs.kamel)
                implementation(project(":lib-ui-compose-videoplayer"))
                implementation(libs.pdfbox)
                implementation(libs.vlcj)
                implementation(project(":lib-cache"))
            }
        }
    }

}

android {
    compileSdk = 34
    namespace = "com.ustadmobile.libuicompose"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = 21
        lint.targetSdk = 34
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}


multiplatformResources {
    multiplatformResourcesPackage = "com.ustadmobile.libuicompose"

    iosBaseLocalizationRegion = "en"
}