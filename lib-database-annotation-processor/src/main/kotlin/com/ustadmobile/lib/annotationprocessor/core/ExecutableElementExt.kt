package com.ustadmobile.lib.annotationprocessor.core

import com.squareup.kotlinpoet.CodeBlock
import java.util.*
import javax.lang.model.element.ExecutableElement

/**
 * An ExecutableElement could be a normal Kotlin function, or it could be a getter method. If it is
 * a Java getter method, then we should access it as .propertyName , otherwise it should be accessed
 * as functionName()
 */
internal fun ExecutableElement.makeAccessorCodeBlock(): CodeBlock {
    val codeBlock = CodeBlock.builder()
    if(this.simpleName.toString().startsWith("get")) {
        codeBlock.add(simpleName.substring(3, 4).toLowerCase(Locale.ROOT) + simpleName.substring(4))
    }else {
        codeBlock.add("$simpleName()")
    }

    return codeBlock.build()
}
