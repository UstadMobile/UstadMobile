import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Properties

//Roughly as per
// https://github.com/JetBrains/compose-multiplatform-desktop-template#readme

//Slightly different to the same module on the all-in-one template, but seems to work better for
//running within the IDE

plugins {
    kotlin("jvm")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.conveyor)
    alias(libs.plugins.license)
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

//Set version and AppConfig properties as Manifest properties.
tasks.withType<Jar> {
    manifest {
        attributes["Ustad-Version"] = rootProject.version

        /*
         * Note manifest keys must replace "." with "-" because "." is not valid to include in as
         * per the JAR Manifest specification.
         *
         * These keys can be uncommented and set if desired
         */
        val buildConfigProps = rootProject.ext["buildConfigProperties"] as? Properties
        if(!buildConfigProps?.getProperty("com.ustadmobile.uilanguages").isNullOrEmpty()) {
            attributes["com-ustadmobile-uilanguages"] = buildConfigProps?.getProperty("com.ustadmobile.uilanguages")
        }

        if(!buildConfigProps?.getProperty("com.ustadmobile.presetlocale").isNullOrEmpty()) {
            attributes["com-ustadmobile-presetlocale"] = buildConfigProps?.getProperty("com.ustadmobile.presetlocale")
        }

        attributes["com-ustadmobile-gopts"] = buildConfigProps?.getProperty("com.ustadmobile.gopts") ?: ""

        attributes["com-ustadmobile-showpoweredbymsg"] = buildConfigProps?.getProperty("com.ustadmobile.showpoweredbymsg") ?: ""

        attributes["com-ustadmobile-apiurl"] = buildConfigProps?.getProperty("com.ustadmobile.apiurl") ?: ""
    }
}

/*
 * Displaying Epubs on Desktop is done by serving the Web Version using the embeddedd server -
 * see LaunchEpubUseCaseJvm for details/rationale.
 */
val bundleWebTask by tasks.register("bundleWeb", Copy::class) {
    dependsOn(":app-react:build")
    from(rootProject.file("app-react/build/dist-web/"))
    into(project.file("app-resources/common/"))
}

val cleanWebBundleTask by tasks.register("cleanWebBundle", Delete::class) {
    delete(project.file("app-resources/common/umapp"))
}

//Required to build proguard release jars for conveyor build.
tasks.named("build").dependsOn("proguardReleaseJars")
tasks.named("clean").dependsOn("cleanWebBundle")
tasks.named("build").dependsOn("bundleWeb")

val copyLicenseReport by tasks.register("copyLicenseReport", Copy::class) {
    from(project.file("build/reports/licenses/licenseReport.html"))
    into(project.file("app-resources/common"))
    rename { "open_source_licenses.html" }
}

tasks.whenObjectAdded {
    if(name == "licenseReport") {
        copyLicenseReport.dependsOn(this)
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(project(":core"))
    implementation(project(":lib-ui-compose"))
    implementation(project(":lib-util"))
    implementation(project(":lib-cache"))
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    implementation(libs.okhttp)
    implementation(libs.quartz)
    implementation(libs.napier)
    implementation(libs.javaffmpeg)

    implementation(libs.moko.resources)
    implementation(libs.moko.resources.compose)
    implementation(libs.precompose)
    implementation(libs.precompose.viewmodel)
    implementation(libs.libphonenumber.google)
    implementation(libs.kamel)
    implementation(libs.ktor.client.okhttp)

    //Not really being used directly, but lack of this class seems to confuse proguard
    implementation(libs.jspecify)
    implementation(libs.apache.commons.pool)
    implementation(libs.apache.commons.dbcp)
    implementation(libs.kodein.di)
    implementation(libs.kodein.kaverit)
    implementation(libs.jcabi.manifests) {
        exclude(group = "org.mockito")
        exclude(group = "org.mockito.kotlin")
    }


    implementation(libs.nanohttpd)
    implementation(libs.xmlpullparsekmp)
    implementation(libs.kxml2)
    implementation(libs.logback.classic)
    implementation(libs.junique)
    implementation(libs.hsqldb)

    //as per https://conveyor.hydraulic.dev/13.0/tutorial/tortoise/2-gradle/#adapting-a-compose-desktop-app
    linuxAmd64(compose.desktop.linux_x64)
    macAmd64(compose.desktop.macos_x64)
    macAarch64(compose.desktop.macos_arm64)
    windowsAmd64(compose.desktop.windows_x64)
}

//As per https://conveyor.hydraulic.dev/13.0/tutorial/tortoise/2-gradle/#adapting-a-compose-desktop-app
configurations.all {
    attributes {
        attribute(Attribute.of("ui", String::class.java), "awt")
    }
}

compose.desktop {
    application {
        //might check https://conveyor.hydraulic.dev/13.0/troubleshooting/troubleshooting-jvm/#localization-doesnt-work-when-packaged
        mainClass = "com.ustadmobile.port.desktop.apprun.AppRunKt"

        //https://blog.jetbrains.com/kotlin/2022/10/compose-multiplatform-1-2-is-out/#proguard
        // https://conveyor.hydraulic.dev/13.0/configs/jvm/#proguard-obfuscation
        // https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Native_distributions_and_local_execution#minification--obfuscation
        buildTypes.release.proguard {
            obfuscate.set(true)
            configurationFiles.from(project.file("compose-desktop.pro"))
        }


        nativeDistributions {
            //As per https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Native_distributions_and_local_execution/README.md#adding-files-to-packaged-application
            appResourcesRootDir.set(project.layout.projectDirectory.dir("app-resources"))

            // https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Native_distributions_and_local_execution/README.md
            modules("java.base")
            modules("java.sql")
            modules("java.naming")

            /*
             * Suggested module jdk.xml.dom not needed and adds 6MB to output size. Others have no
             * size impact.
             */
            modules("java.compiler")
            modules("java.instrument")
            modules("java.management")
            modules("java.rmi")
            modules("jdk.unsupported")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi)
            packageVersion = "1.0.0"
            packageName = "UstadMobile"
            version = rootProject.version
            description = "Ustad Mobile"
            copyright = "© UstadMobile FZ-LLC."
            licenseFile.set(rootProject.file("LICENSE"))


            windows {
                packageVersion = "1.0.0"
                msiPackageVersion = "1.0.0"
                exePackageVersion = "1.0.0"
                iconFile.set(project.file("icon.ico"))
            }

            /**
             * NOTE: On Ubuntu, the icon will update ONLY if the class name of the app is changed.
             */
            linux {
                iconFile.set(project.file("icon.png"))
            }
        }
    }
}

licenseReport {
    generateHtmlReport = true
}

