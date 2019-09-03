package com.ustadmobile.lib.annotationprocessor.core

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

internal fun TypeElement.asEntityTypeSpecBuilder(): TypeSpec.Builder {
    val typeSpecBuilder = TypeSpec.classBuilder(this.simpleName.toString())
    this.enclosedElements
            .filter { it.kind == ElementKind.FIELD
                    && it.simpleName.toString() != "Companion"
                    && Modifier.STATIC !in it.modifiers }
            .forEach {
        val propSpec = PropertySpec.builder(it.simpleName.toString(),
                it.asType().asTypeName().javaToKotlinType())
        propSpec.addAnnotations( it.annotationMirrors.map { AnnotationSpec.get(it) })
        typeSpecBuilder.addProperty(propSpec.build())
    }

    return typeSpecBuilder
}

internal fun TypeElement.asEntityTypeSpec() = this.asEntityTypeSpecBuilder().build()
