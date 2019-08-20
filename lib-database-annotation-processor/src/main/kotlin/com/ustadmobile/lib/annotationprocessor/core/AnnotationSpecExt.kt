package com.ustadmobile.lib.annotationprocessor.core

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock

/**
 * Searches a list of the codeblocks that are associated with an annotation spec.
 */
fun List<CodeBlock>.findBooleanMemberValue(memberName: String): Boolean? = this
        .map { it.toString().trim() }.firstOrNull { it.startsWith(memberName) }?.endsWith("true")