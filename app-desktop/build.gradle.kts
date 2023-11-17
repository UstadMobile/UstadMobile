import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

//Roughly as per
// https://github.com/JetBrains/compose-multiplatform-desktop-template#readme

//Slightly different to the same module on the all-in-one template, but seems to work better for
//running within the IDE

plugins {
    kotlin("jvm")
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    jvmToolchain(17)
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(project(":core"))
    implementation(project(":lib-ui-compose"))
    implementation(project(":lib-util"))
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    implementation(libs.okhttp)
    implementation(libs.quartz)

    api(libs.moko.resources)
    api(libs.moko.resources.compose)
    api(libs.precompose)
    api(libs.precompose.viewmodel)
}

compose.desktop {
    application {
        mainClass = "com.ustadmobile.port.desktop.AppKt"
    }
}
