import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//Roughly as per
// https://github.com/JetBrains/compose-multiplatform-desktop-template#readme

//Slightly different to the same module on the all-in-one template, but seems to work better for
//running within the IDE

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version "1.4.3"
}

kotlin {
    jvmToolchain(17)
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(project(":core"))
    implementation(project(":lib-ui-compose"))
    api(libs.moko.resources)
    api(libs.moko.resources.compose)

}

compose.desktop {
    application {
        mainClass = "com.ustadmobile.port.desktop.AppKt"
    }
}
