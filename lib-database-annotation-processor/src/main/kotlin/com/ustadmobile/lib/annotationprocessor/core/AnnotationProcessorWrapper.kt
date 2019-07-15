package com.ustadmobile.lib.annotationprocessor.core

import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@SupportedAnnotationTypes("androidx.room.Database")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(value = [DbProcessorJdbcKotlin.OPTION_OUTPUT_DIR,
    DbProcessorKtorServer.OPTION_KTOR_OUTPUT, DbProcessorRepository.OPTION_OUTPUT_DIR])
class AnnotationProcessorWrapper: AbstractProcessor() {

    val processors = listOf(DbProcessorJdbcKotlin(), DbProcessorKtorServer(),
            DbProcessorRepository(), DbProcessorSync())

    override fun init(p0: ProcessingEnvironment?) = processors.forEach { it.init(p0) }

    override fun process(p0: MutableSet<out TypeElement>?, p1: RoundEnvironment?): Boolean {
        processors.forEach { it.process(p0, p1) }

        return true
    }

}