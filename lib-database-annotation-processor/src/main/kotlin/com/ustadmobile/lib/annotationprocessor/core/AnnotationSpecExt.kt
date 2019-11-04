package com.ustadmobile.lib.annotationprocessor.core

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import org.apache.commons.text.StringEscapeUtils

/**
 * Searches a list of the codeblocks that are associated with an annotation spec.
 */
fun List<CodeBlock>.findBooleanMemberValue(memberName: String): Boolean? = this
        .map { it.toString().trim() }.firstOrNull { it.startsWith(memberName) }?.endsWith("true")

/**
 * Get the string value of an AnnotationSpec member (e.g. the query for @Query("SELECT ...")). This
 * is useful where a FunSpec type has been used and we want to get the query
 */
fun AnnotationSpec.valueMemberToString(): String {
    var strValue = members
        .first { it.toString().trim().startsWith("value") || it.toString().trim().startsWith("\"") }.toString()

    if(strValue.endsWith("trimMargin()")) {
        strValue = strValue.removeSuffix(".trimMargin()")
                .removeSurrounding("\"\"\"").trimMargin()
    }else {
        strValue = strValue.removeSurrounding("\"")
    }

    strValue = StringEscapeUtils.unescapeJava(strValue)

    return strValue
}
