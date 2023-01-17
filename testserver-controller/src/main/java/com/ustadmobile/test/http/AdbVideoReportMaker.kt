package com.ustadmobile.test.http

import com.ustadmobile.lib.util.SysPathUtil
import com.ustadmobile.test.http.junitxml.TestSuites
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import java.io.File
import java.io.FileFilter
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.DateFormat
import java.util.*

data class DummyObject(val str: String)

fun main(args: Array<String>) {
    val projectRootDir = File(args[0])
    val adbPath = SysPathUtil.findCommandInPath(
        commandName = "adb",
        extraSearchPaths = System.getenv("ANDROID_HOME") ?: "",
    ) ?: throw IllegalStateException("Cannot find adb!")

    val e2eTestNames = File(
        projectRootDir, "test-end-to-end/android-maestro/e2e-tests/"
    ).listFiles(FileFilter {
        it.extension == "yaml"
    })?.map {
        it.nameWithoutExtension
    }?.sorted() ?: emptyList()


    val resultsDir = File(projectRootDir, "test-end-to-end/android-maestro/build/results/")
    val devices = resultsDir.listFiles(FileFilter {
        it.isDirectory
    })?.map {
        it.name
    } ?: emptyList()


    generateReport(projectRootDir, adbPath, resultsDir, devices, e2eTestNames)

}

/**
 * Generate a simple HTML file to display videos. This requires the directory structure to be as
 * mentioned in test-end-to-end/android-maestro/README.md .
 *
 * @param projectDir root source code directory
 * @param adbPath path to the adb command
 * @param destDir report output directory normally test-end-to-end/android-maestro/build/results
 * @param devices list of emulator serials that ran the tests
 *  param testNames list of end-to-end tests run as per the YAML file names in
 *  test-end-to-end/android-maestro/e2e-tests-descriptions-override (without the .yaml extension)
 *
 * A description for an end-to-end test can be saved to e2e-tests/testname-description.txt . If this
 * is not available, the reporter will try and generate this based on the test-end-to-end/README.md
 * file.
 */
fun generateReport(
    projectDir: File,
    adbPath: File,
    destDir: File,
    devices: List<String>,
    testNames: List<String>,
) {
    val xml = XML {
        autoPolymorphic = true
    }

    val testSuites = devices.map { device ->
        val resultXmlFile = File(projectDir, "test-end-to-end/android-maestro/build/results/$device/report.xml")
        val testSuites = if(resultXmlFile.exists()) {
            xml.decodeFromString<TestSuites>(resultXmlFile.readText())
        }else {
            TestSuites(emptyList())
        }
        device to testSuites
    }.toMap()

    val readMeLines = File(projectDir, "test-end-to-end/README.md").readLines()

    destDir.mkdirs() //this should have already been created, but would be empty if no tests have been recorded
    FileOutputStream(File(destDir, "adbscreenrecord.css")).use { fileOut ->
        DummyObject::class.java.getResourceAsStream("/adbscreenrecord.css")
            ?.use { resourceIn ->
                resourceIn.copyTo(fileOut)
                fileOut.flush()
            }
    }


    val fileWriter = FileWriter(File(destDir, "index.html"))

    fileWriter.appendHTML().html {
        head {
            link(href = "adbscreenrecord.css", rel = "stylesheet", type = "text/css")
        }

        body {
            h2 {
                +"End-to-end Android Testing"
            }
            div(classes = "subtitle") {
                span(classes = "projectname") {
                    +"End To End Test Report "
                }
                span(classes = "timestamp") {
                    +DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(Date())
                }
            }

            table {
                tr {
                    td {
                        +"  "
                    }

                    devices.forEach { deviceName ->
                        val getPropArgs = listOf(adbPath.absolutePath, "-s", deviceName,
                            "shell", "getprop")
                        val androidVersion = ProcessBuilder(getPropArgs + listOf("ro.build.version.release"))
                            .start().getOutputAsString()
                        val manufacturer = ProcessBuilder(getPropArgs + listOf("ro.product.manufacturer"))
                            .start().getOutputAsString()
                        val model = ProcessBuilder(getPropArgs + listOf("ro.product.model"))
                            .start().getOutputAsString()


                        td(classes = "devicetd") {
                            +deviceName
                            br { }
                            +"Android $androidVersion"
                            br { }
                            + "$manufacturer / $model"
                        }
                    }
                }



                testNames.forEach { testName ->
                    val descriptionFile = File(projectDir,
                        "test-end-to-end/android-maestro/e2e-tests-descriptions-override/$testName.txt")
                    val descriptionText = if(descriptionFile.exists()) {
                        descriptionFile.readText()
                    }else {
                        val firstUnderscoreIndex = testName.indexOf("_")
                        val testSectionNum = testName.substring(0, firstUnderscoreIndex).toIntOrNull()
                        val testSubsectionNum = testName.substring(firstUnderscoreIndex + 1)
                            .substringBefore("_").toIntOrNull()
                        val lineStart = "$testSectionNum.$testSubsectionNum"
                        val descriptionFirstLineIndex = readMeLines.indexOfFirst { it.startsWith(lineStart) }
                        val remainingLines = readMeLines.subList(descriptionFirstLineIndex + 1,
                            readMeLines.size)
                        val descriptionEndLine = remainingLines.indexOfFirst { it.isBlank() || it.isEmpty() } + descriptionFirstLineIndex

                        if(descriptionFirstLineIndex > 0) {
                            readMeLines.subList(descriptionFirstLineIndex, descriptionEndLine + 1)
                                .joinToString(separator = " ")
                        } else {
                            ""
                        }
                    }



                    tr {
                        td(classes = "methodtd") {
                            + descriptionText
                        }

                        devices.forEach { deviceName ->
                            val testCase = testSuites[deviceName]?.testSuites
                                ?.flatMap { it.testCases }?.firstOrNull { it.id == testName }
                            val tdCssClass = when {
                                testCase?.failure?.isEmpty() == true -> "pass"
                                testCase?.failure?.isNotEmpty() == true -> "fail"
                                else -> "notrun"
                            }

                            val deviceTestDir = File(File(destDir, deviceName), testName)
                            td(classes = "testvideo $tdCssClass") {
                                val videoFile = File(deviceTestDir, "screenrecord.mp4")
                                if (videoFile.exists()) {
                                    video {
                                        src = "$deviceName/$testName/screenrecord.mp4"
                                        controls = true
                                        attributes["preload"] = "none"

                                        val coverImageFile = File(deviceTestDir,
                                            "screenrecord-poster.png")
                                        if (coverImageFile.exists()) {
                                            poster = "$deviceName/$testName/screenrecord-poster.png"
                                        }
                                    }
                                } else {
                                    +"[No video]"
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    fileWriter.flush()
    fileWriter.close()
}
