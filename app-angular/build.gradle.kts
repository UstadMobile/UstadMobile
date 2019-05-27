//Script copied from https://github.com/svok/kotlin-multiplatform-sample/blob/master/proj-angularfront/build.gradle.kts
import com.moowork.gradle.node.yarn.YarnTask

plugins {
  id("kotlin-platform-js")
  id("com.moowork.node") version "1.3.1"
  id("com.bmuschko.docker-remote-api") version "4.8.1"
  id("com.crowdproj.plugins.jar2npm") version "1.0.1"
}

node {
  download = true
  workDir = file("${project.buildDir}/node")
  npmWorkDir = file("${project.buildDir}/node")
  yarnWorkDir = file("${project.buildDir}/node")
  nodeModulesDir = file("${project.projectDir}")
}

tasks {

  withType<Jar> {
    dependsOn("ngBuild")
    archiveBaseName.set(project.name)
  }

  val ngBuild = task<YarnTask>("ngBuild") {
    dependsOn("yarn_install")

    inputs.files(fileTree("node_modules"))
    inputs.files(fileTree("src"))
    inputs.file("angular.json")
    inputs.file("package.json")

    outputs.dir("dist")

    args = listOf("run", "build")
  }

 register("webdriverUpdate", YarnTask::class) {
    args = listOf("run", "update-driver")
  }

  task<YarnTask>("ngTest") {
    dependsOn("yarn_install")
    dependsOn("webdriverUpdate")
    args = listOf("run", "testPhantom")
  }

  task<YarnTask>("serve") {
    args = listOf("run", "start")
  }


  clean.get().doLast {
    println("Delete dist and node_modules")
    file("$projectDir/dist").deleteRecursively()
    file("$projectDir/node_modules").deleteRecursively()
  }

  compileKotlin2Js {
    kotlinOptions {
      metaInfo = true
      outputFile = "${project.buildDir.path}/js/index.js"
      sourceMap = true
      sourceMapEmbedSources = "always"
      moduleKind = "commonjs"
      main = "call"
    }
  }

  compileTestKotlin2Js {
    kotlinOptions {
      metaInfo = true
      outputFile = "${project.buildDir.path}/js-tests/${project.name}-tests.js"
      sourceMap = true
      moduleKind = "commonjs"
      main = "call"
    }
  }

  ngBuild.dependsOn(jar2npm)
}

dependencies {
  implementation(project(":core"))
  implementation(project(":lib-database"))
}
