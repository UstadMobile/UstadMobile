package com.ustadmobile.lib.annotationprocessor.core

import androidx.room.Database
import java.io.*
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import javax.tools.StandardLocation

class DbProcessorAndroid: AbstractDbProcessor() {

    override fun process(elements: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        //find all databases on the source path

        roundEnv.getElementsAnnotatedWith(Database::class.java).map {it as TypeElement}.forEach {dbEl ->
            val sourceFile = processingEnv.filer.getResource(StandardLocation.SOURCE_PATH,
                    pkgNameOfElement(dbEl, processingEnv), "${dbEl.simpleName}.kt")

            val outDirs = processingEnv.options[AnnotationProcessorWrapper.OPTION_ANDROID_OUTPUT]
                    ?.split(File.pathSeparatorChar) ?: listOf<String>()

            val relativePath = pkgNameOfElement(dbEl, processingEnv).replace(".", File.separator) +
                    "/${dbEl.simpleName}.kt"
            val outFiles = outDirs.map { File(it, relativePath) }
            outFiles.filter { !it.parentFile.exists() }.forEach {
                if(!it.parentFile.mkdirs()) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "Could not create android output " +
                            "dir for ${dbEl.qualifiedName} : ${it.parentFile.absolutePath}")
                    return true
                }
            }

            val outStreams = outFiles.map { BufferedWriter(FileWriter(it)) }

            BufferedReader(sourceFile.openReader(true)).use {
                var line = null as String?
                do {
                    line = it.readLine()
                    var lineOut = ""
                    if(line.contains("//#DOORDB_TRACKER_ENTITIES")) {

                    }else {
                        lineOut = line
                    }

                    outStreams.forEach {
                        it.write(lineOut)
                        it.newLine()
                    }
                }while (line != null)
            }
        }


        return true
    }
}