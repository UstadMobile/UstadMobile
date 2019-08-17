package com.ustadmobile.lib.annotationprocessor.core

import androidx.room.Database
import com.ustadmobile.lib.annotationprocessor.core.AnnotationProcessorWrapper.Companion.OPTION_ANDROID_OUTPUT
import com.ustadmobile.lib.annotationprocessor.core.AnnotationProcessorWrapper.Companion.OPTION_SOURCE_PATH
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorSync.Companion.SUFFIX_SYNCDAO_ABSTRACT
import io.ktor.util.extension
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

class DbProcessorAndroid: AbstractDbProcessor() {

    private fun findSourceFile(packageName: String, className: String): File? {
        val srcPaths = processingEnv.options[OPTION_SOURCE_PATH]
        if(srcPaths == null) {
            messager.printMessage(Diagnostic.Kind.ERROR, "DoorDbProcessorAndroid: must set ${OPTION_SOURCE_PATH}")
            return null
        }

        val packageRelPath = "${packageName.replace(".", File.separator)}${File.separator}${className}.kt"
        return srcPaths.split(File.pathSeparator).map { File(it, packageRelPath)}
                .firstOrNull { it.exists() }
    }

    private fun adjustDbFile(dbTypeEl: TypeElement, inFile: File, outDirs: List<String>) {
        val relativePath = pkgNameOfElement(dbTypeEl, processingEnv).replace(".", File.separator) +
                "/${dbTypeEl.simpleName}.kt"

        val outFiles = outDirs.map { File(it, relativePath) }
        outFiles.filter { !it.parentFile.exists() }.forEach {
            if(!it.parentFile.mkdirs()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Could not create android output " +
                        "dir for ${dbTypeEl.qualifiedName} : ${it.parentFile.absolutePath}")
                throw IOException("Could not create android output dir ${it.parentFile.absolutePath}")
            }
        }

        val outStreams = outFiles.map { BufferedWriter(FileWriter(it)) }

        BufferedReader(FileReader(inFile)).use {

            for(line in it.lines()) {
                var lineOut = ""
                if (line.contains("//#DOORDB_TRACKER_ENTITIES")) {
                    lineOut += "//Generated section: add tracker entities to room database\n"
                    lineOut += syncableEntityTypesOnDb(dbTypeEl,processingEnv)
                            .joinToString(prefix = ",") { "${it.simpleName}_trk::class" }
                    lineOut += "\n//End of generated section: add tracker entities to room database\n"
                } else if(line.contains("//#DOORDB_SYNCDAO")) {
                    lineOut = "abstract fun _syncDao(): ${dbTypeEl.simpleName}${SUFFIX_SYNCDAO_ABSTRACT}"
                }else if(line.contains("@JsName") || line.contains("kotlin.js.JsName"))
                    lineOut = ""
                else {
                    lineOut = line
                }

                outStreams.forEach {
                    it.write(lineOut)
                    it.newLine()
                }
            }
        }

        outStreams.forEach {
            it.flush()
            it.close()
        }

        messager.printMessage(Diagnostic.Kind.NOTE, "DbProcessorAndroid: wrote adjusted version of" +
                " ${dbTypeEl.simpleName} to ${outDirs.joinToString()}")
    }

    override fun process(elements: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        //find all databases on the source path
        val outDirsArg = processingEnv.options[OPTION_ANDROID_OUTPUT]
        if(outDirsArg == null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "DoorDb Android output not specified: please provide $OPTION_ANDROID_OUTPUT " +
                            "annotation processor argument")
            return true
        }

        val srcPathsArg = processingEnv.options[OPTION_SOURCE_PATH]
        if(srcPathsArg == null) {
            messager.printMessage(Diagnostic.Kind.ERROR, "DoorDbProcessorAndroid: must set ${OPTION_SOURCE_PATH}")
            return false
        }

        val srcPaths = srcPathsArg.split(File.pathSeparator)
        val outDirs = outDirsArg.split(File.pathSeparator)
        val outPaths = outDirs.map { Paths.get(File(it).toURI()) }

        srcPaths.map { Paths.get(File(it).toURI()) }.forEach{srcPath ->
            messager.printMessage(Diagnostic.Kind.NOTE, "Walk src path: $srcPath")
            Files.walk(srcPath).forEach {srcFilePath ->
                messager.printMessage(Diagnostic.Kind.NOTE, "Check: $srcFilePath")
                if(srcFilePath.extension == "kt"){
                    val srcRelativePath = srcPath.relativize(srcFilePath)
                    val outFilePaths = outPaths.map { it.resolve(srcRelativePath)}
                    if(outFilePaths.map {it.toFile() }
                                    .any { !it.exists() || it.lastModified() < srcFilePath.toFile().lastModified()}) {
                        val lines = srcFilePath.toFile().readLines()
                        if(!lines.any { it.contains("@Database" )} ) {
                            outFilePaths.forEach {
                                val parentDirFile = it.parent.toFile()
                                if(!parentDirFile.isDirectory){
                                    parentDirFile.mkdirs()
                                }

                                var srcFileIn = null as BufferedReader?
                                var fileOut = null as BufferedWriter?

                                try {
                                    srcFileIn = BufferedReader(FileReader(srcFilePath.toFile()))
                                    fileOut = BufferedWriter(FileWriter(it.toFile()))


                                    for(line in srcFileIn.lines()) {
                                        if(!line.contains("@JsName") && !line.contains("kotlin.js.JsName"))
                                            fileOut.write(line)

                                        fileOut.newLine()
                                    }
                                }catch(e: IOException) {
                                    messager.printMessage(Diagnostic.Kind.ERROR, "IOException " +
                                            "copying db source file$srcFilePath to $it : $e")
                                }finally {
                                    srcFileIn?.close()
                                    fileOut?.flush()
                                    fileOut?.close()
                                }

                                messager.printMessage(Diagnostic.Kind.NOTE, "DbProcessorAndroid: Copy: $srcFilePath -> $it")
                            }
                        }
                    }
                }

            }
        }

        val dbElements = roundEnv.getElementsAnnotatedWith(Database::class.java).map {it as TypeElement}
        dbElements.forEach {dbEl ->
            val sourceFile = findSourceFile(pkgNameOfElement(dbEl, processingEnv), dbEl.simpleName.toString())

            if(sourceFile != null) {
                adjustDbFile(dbEl, sourceFile, outDirs)
            }
        }


        return true
    }
}