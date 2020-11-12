package com.ustadmobile.lib.annotationprocessor.core

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import org.apache.commons.text.StringEscapeUtils
import androidx.room.Query
import com.squareup.kotlinpoet.asClassName

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

/**
 * Where the given list of annotation specs contains a Query annotation, get the
 * query SQL as a String
 */
fun List<AnnotationSpec>.daoQuerySql() : String {
    val queryValueMember = first { it.className == Query::class.asClassName() }.valueMemberToString()
    return queryValueMember.trim().removePrefix("value").trim().removePrefix("=")
            .trim().removeAllPrefixedInstancesOf("\"").removeAllSuffixedInstancesOf("\"")
}
