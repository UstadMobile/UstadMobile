package com.ustadmobile.lib.annotationprocessor.core

import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedAnnotationTypes("androidx.room.Database")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(value = [DbProcessorJdbcKotlin.OPTION_OUTPUT_DIR,
    DbProcessorKtorServer.OPTION_KTOR_OUTPUT, DbProcessorRepository.OPTION_OUTPUT_DIR,
    DbProcessorSync.OPTION_IMPL_OUTPUT_DIR, DbProcessorSync.OPTION_ABSTRACT_OUTPUT_DIR])
class AnnotationProcessorWrapper: AbstractProcessor() {

    val processors = listOf(DbProcessorJdbcKotlin(), DbProcessorKtorServer(),
            DbProcessorRepository(), DbProcessorSync())

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

}