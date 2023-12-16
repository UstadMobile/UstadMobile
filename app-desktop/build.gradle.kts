import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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
    implementation(libs.napier)

    api(libs.moko.resources)
    api(libs.moko.resources.compose)
    api(libs.precompose)
    api(libs.precompose.viewmodel)
    implementation(libs.libphonenumber.google)
    implementation(libs.kamel)
}

compose.desktop {
    application {
        mainClass = "com.ustadmobile.port.desktop.AppKt"

        nativeDistributions {
            modules("java.sql")
            modules("java.base")
            modules("java.compiler")
            modules("java.instrument")
            modules("java.management")
            modules("java.naming")
            modules("java.rmi")
            modules("jdk.unsupported")
            modules("jdk.xml.dom")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi)
            packageVersion = "1.0.0"
            packageName = "UstadMobile"
            version = rootProject.version
            description = "Ustad Mobile"
            copyright = "© UstadMobile FZ-LLC."
            vendor = "UstadMobile FZ-LLC"
            licenseFile.set(rootProject.file("LICENSE"))
            windows {
                packageVersion = "1.0.0"
                msiPackageVersion = "1.0.0"
                exePackageVersion = "1.0.0"
                iconFile.set(project.file("ustad-logo.ico"))
            }
        }
    }
}
