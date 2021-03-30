
plugins {
    id ("org.jetbrains.kotlin.js")
}

val group = rootProject.group
val version = rootProject.version

repositories {
    jcenter()
    mavenCentral()
    maven ("https://dl.bintray.com/kotlin/kotlin-js-wrappers")

}

kotlin {
    js(LEGACY) {
        browser {
            binaries.executable()
            webpackTask {
                cssSupport.enabled = true
            }
            runTask {
                cssSupport.enabled = true
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
    }
}

dependencies {
    testImplementation ("org.jetbrains.kotlin:kotlin-test-js")
    implementation ("org.jetbrains","kotlin-react",
        rootProject.ext["version_kotlin_react"].toString())
    implementation ("org.jetbrains","kotlin-react-router-dom",
        rootProject.ext["version_kotlin_react_router_dom"].toString())
    implementation ("org.jetbrains","kotlin-react-dom",
        rootProject.ext["version_kotlin_react"].toString())
    implementation ("org.jetbrains","kotlin-styled",
        rootProject.ext["version_kotlin_styled"].toString())
    implementation ("org.jetbrains","kotlin-redux",
        rootProject.ext["version_kotlin_redux"].toString())
    implementation ("org.jetbrains","kotlin-react-redux",
        rootProject.ext["version_kotlin_react_redux"].toString())
    implementation ("com.ccfraser.muirwik","muirwik-components",
        rootProject.ext["version_muirwik_components"].toString())
}

