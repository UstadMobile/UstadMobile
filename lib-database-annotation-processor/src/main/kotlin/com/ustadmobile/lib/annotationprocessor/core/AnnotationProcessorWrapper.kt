package com.ustadmobile.lib.annotationprocessor.core

import com.ustadmobile.lib.annotationprocessor.core.AnnotationProcessorWrapper.Companion.OPTION_ANDROID_OUTPUT
import com.ustadmobile.lib.annotationprocessor.core.AnnotationProcessorWrapper.Companion.OPTION_JVM_DIRS
import com.ustadmobile.lib.annotationprocessor.core.AnnotationProcessorWrapper.Companion.OPTION_KTOR_OUTPUT
import com.ustadmobile.lib.annotationprocessor.core.AnnotationProcessorWrapper.Companion.OPTION_SOURCE_PATH
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedAnnotationTypes("androidx.room.Database")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(value = [OPTION_JVM_DIRS, OPTION_ANDROID_OUTPUT, OPTION_KTOR_OUTPUT,
    OPTION_SOURCE_PATH, "kapt.kotlin.generated"])
class AnnotationProcessorWrapper: AbstractProcessor() {

    val processors = listOf(DbProcessorJdbcKotlin(), DbProcessorKtorServer(),
            DbProcessorRepository(), DbProcessorSync(), DbProcessorAndroid())

    lateinit var messager: Messager

    override fun init(p0: ProcessingEnvironment) {
        messager = p0.messager
        processors.forEach { it.init(p0) }
    }

    override fun process(p0: MutableSet<out TypeElement>?, p1: RoundEnvironment?): Boolean {
        processors.forEach {
            messager.printMessage(Diagnostic.Kind.NOTE, "Running processor: ${it.javaClass.simpleName}")
            it.process(p0, p1)
            messager.printMessage(Diagnostic.Kind.NOTE, "Finished running processor: ${it.javaClass.simpleName}")
        }

        return true
    }

    companion object {

        const val OPTION_SOURCE_PATH = "doordb_source_path"

        const val OPTION_JVM_DIRS = "doordb_jvm_out"

        const val OPTION_ANDROID_OUTPUT = "doordb_android_out"

        const val OPTION_KTOR_OUTPUT = "doordb_ktor_out"
    }

}