package com.ustadmobile.lib.annotationprocessor.core

import com.squareup.kotlinpoet.*
import java.util.*
import javax.lang.model.element.ExecutableElement

/**
 * Add a method or property that overrides the given accessor. The ExecutableElement could be a
 * getter method - in which case we need to add a Kotlin property with a getter method. Otherwise we
 * add an overriding function
 */
fun TypeSpec.Builder.addAccessorOverride(methodName: String, returnType: TypeName, codeBlock: CodeBlock) {
    if(methodName.startsWith("get")) {
        val propName = methodName.substring(3, 4).toLowerCase(Locale.ROOT) + methodName.substring(4)
        val getterFunSpec = FunSpec.getterBuilder().addCode(codeBlock)
        addProperty(PropertySpec.builder(propName, returnType,
                KModifier.OVERRIDE).getter(getterFunSpec.build()).build())
    }else {
        addFunction(FunSpec.builder(methodName)
                .addModifiers(KModifier.OVERRIDE)
                .returns(returnType)
                .addCode(codeBlock)
                .build())
    }
}

fun TypeSpec.Builder.addAccessorOverride(executableElement: ExecutableElement, codeBlock: CodeBlock)  =
        addAccessorOverride(executableElement.simpleName.toString(), executableElement.returnType.asTypeName(), codeBlock)
