import org.jetbrains.compose.compose

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    jvmToolchain(17)

    jvm("desktop")

    sourceSets {
        named("commonMain") {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
            }
        }
        named("desktopMain") {
            dependencies {
                implementation(libs.vlcj)
            }
        }
    }
}
