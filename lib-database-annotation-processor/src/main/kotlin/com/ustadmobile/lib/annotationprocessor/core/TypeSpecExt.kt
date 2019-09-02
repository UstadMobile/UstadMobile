package com.ustadmobile.lib.annotationprocessor.core

import com.squareup.kotlinpoet.*
import java.util.*
import javax.lang.model.element.ExecutableElement

/**
 * Add a method or property that overrides the given accessor. The ExecutableElement could be a
 * getter method - in which case we need to add a Kotlin property with a getter method. Otherwise we
 * add an overriding function
 */
fun TypeSpec.Builder.addAccessorOverride(executableEl: ExecutableElement, codeBlock: CodeBlock) {
    if(executableEl.simpleName.toString().startsWith("get")) {
        val propName = executableEl.simpleName.substring(3, 4).toLowerCase(Locale.ROOT) + executableEl.simpleName.substring(4)
        val getterFunSpec = FunSpec.getterBuilder().addCode(codeBlock)
        addProperty(PropertySpec.builder(propName, executableEl.returnType.asTypeName(),
                KModifier.OVERRIDE).getter(getterFunSpec.build()).build())
    }else {
        addFunction(FunSpec.overriding(executableEl)
                .addCode(codeBlock)
                .build())
    }
}