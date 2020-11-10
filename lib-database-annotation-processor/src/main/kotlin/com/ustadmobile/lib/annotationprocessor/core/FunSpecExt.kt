package com.ustadmobile.lib.annotationprocessor.core

import androidx.room.Query
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.asClassName
import javax.annotation.processing.ProcessingEnvironment

/**
 * Simple shorthand function to check if the given function spec
 * contains the given annotation
 */
fun <A: Annotation> FunSpec.hasAnnotation(annotationClass: Class<A>) : Boolean {
    return annotations.any { it.className == annotationClass.asClassName() }
}

/**
 * Where this function represents a DAO function with a query, get the query SQL
 */
fun FunSpec.daoQuerySql() = annotations.daoQuerySql()

/**
 * Determines if this FunSpec represents a DAO query function that where the return result
 * includes syncable entities
 */
fun FunSpec.isQueryWithSyncableResults(processingEnv: ProcessingEnvironment) =
        hasAnnotation(Query::class.java) &&
        returnType?.hasSyncableEntities(processingEnv) == true