package com.ustadmobile.lib.annotationprocessor.core

import java.util.*

/**
 * Determine if the given SQL runs a query that could modify a table. This will return true
 * for insert, update, delete, and replace. It will return false for other (e.g. select) queries
 */
fun String.isSQLAModifyingQuery() : Boolean {
    val queryTrim = toLowerCase(Locale.ROOT).trim()
    return listOf("update", "delete", "insert", "replace").any { queryTrim.startsWith(it) }
}



