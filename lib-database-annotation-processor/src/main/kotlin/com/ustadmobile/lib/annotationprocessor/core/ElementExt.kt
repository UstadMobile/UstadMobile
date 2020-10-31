package com.ustadmobile.lib.annotationprocessor.core

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

/**
 * Get the qualified package name of the given element as a string
 */
fun Element.qualifiedPackageName(processingEnv: ProcessingEnvironment): String =
        processingEnv.elementUtils.getPackageOf(this).qualifiedName.toString()
