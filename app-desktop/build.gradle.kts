import org.jetbrains.compose.desktop.application.dsl.TargetFormat

//Roughly as per
// https://github.com/JetBrains/compose-multiplatform-desktop-template#readme

//Slightly different to the same module on the all-in-one template, but seems to work better for
//running within the IDE

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version "1.4.3"
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(project(":lib-ui-compose"))
    implementation(project(":core"))
}

compose.desktop {
    application {
        mainClass = "com.ustadmobile.port.desktop.AppKt"
    }
}
