plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
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
                api(libs.door.runtime)
                compileOnly(libs.door.room.annotations)
                implementation(libs.kotlinx.serialization)
                implementation(libs.coroutines)
                implementation(libs.ktor.client.core)


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

            }
        }

        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {

            }
        }

        val androidMain by getting {
            dependencies {
                api(libs.androidx.room.runtime)
                api(libs.androidx.room.ktx)
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
dependencies {
    add("kspJvm", libs.door.compiler)
    add("kspJs", libs.door.compiler)
    add("kspAndroid", libs.door.compiler)
    add("kspAndroid", libs.androidx.room.compiler)
}

ksp {


}