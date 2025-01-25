plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.atomicfu)
    alias(libs.plugins.serialization)
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
                api(project(":lib-ihttp-core"))
                implementation(libs.coroutines)
                implementation(libs.door.runtime)
                implementation(libs.atomicfu)
                implementation(libs.kotlinxio.core)
                implementation(libs.ktor.client.core)
                implementation(libs.napier)
                implementation(libs.kotlinx.serialization)
                compileOnly(libs.door.room.annotations)
            }
        }


        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
            }
        }

        val commonJvmMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(project(":lib-ihttp-okhttp"))
                implementation(libs.okhttp)
            }
        }

        val jvmMain by getting {
            dependsOn(commonJvmMain)
            dependencies {
                implementation(libs.okhttp)
            }
        }

        val jvmTest by getting {
            dependsOn(commonTest)
            dependencies {
                implementation(libs.mockwebserver)
                implementation(libs.mockito.kotlin)
                implementation(libs.turbine)
                implementation(project(":lib-test-common"))
            }
        }

        val androidMain by getting {
            dependsOn(commonJvmMain)

            dependencies {
                implementation(libs.androidx.room.ktx)
                implementation(libs.androidx.room.runtime)
            }
        }

    }
}

dependencies {
    implementation(project(":lib-ihttp-core"))
    add("kspJvm", libs.door.compiler)
    add("kspAndroid", libs.door.compiler)
    add("kspAndroid", libs.androidx.room.compiler)

    coreLibraryDesugaring(libs.android.desugar.libs)
}

android {
    compileSdk = 34
    namespace = "com.ustadmobile.libcache"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = 21
        targetSdk = 34
        multiDexEnabled = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlin {
        jvmToolchain(17)
    }
}
