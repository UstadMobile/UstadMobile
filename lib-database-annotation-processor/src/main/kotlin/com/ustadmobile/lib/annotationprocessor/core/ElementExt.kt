package com.ustadmobile.lib.annotationprocessor.core

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.PackageElement

/**
 * Get the qualified package name of the given element as a string
 */
fun Element.qualifiedPackageName(processingEnv: ProcessingEnvironment): String =
        processingEnv.elementUtils.getPackageOf(this).qualifiedName.toString()

/**
 * Shorthand to check if the given element has an annotation or not
 */
fun <A: Annotation> Element.hasAnnotation(annotation: Class<A>): Boolean = this.getAnnotation(annotation) != null

/**
 * Shorthand to get the package name of a given element
 */
val Element.packageName : String
    get() {
        var el = this
        while (el.kind != ElementKind.PACKAGE) {
            el = el.enclosingElement
        }

        return (el as PackageElement).qualifiedName.toString()
    }
