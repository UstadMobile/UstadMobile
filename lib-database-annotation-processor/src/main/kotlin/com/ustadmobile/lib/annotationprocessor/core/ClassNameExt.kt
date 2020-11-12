package com.ustadmobile.lib.annotationprocessor.core

import androidx.room.Embedded
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.ustadmobile.door.annotation.SyncableEntity
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic

/**
 * Replaces AbstractDbProcessor.findEntitiesWithAnnotation
 *
 * This searches the given entity itself and all EmbeddedEntities
 */
fun ClassName.findAllEntitiesWithAnnotation(annotationClass: Class<out Annotation>,
                               processingEnv: ProcessingEnvironment,
                               embedPath: List<String> = listOf()): Map<List<String>, ClassName> {
    if(this in QUERY_SINGULAR_TYPES)
        return mapOf()


    val entityTypeEl = processingEnv.elementUtils.getTypeElement(canonicalName)
    if(entityTypeEl == null){
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                "findAllEntitiesWithAnnotation cannot find : " + canonicalName)
        return mapOf()
    }

    val syncableEntityList = mutableMapOf<List<String>, ClassName>()

    entityTypeEl.ancestorsAsList(processingEnv).forEach {
        if(it.getAnnotation(annotationClass) != null)
            syncableEntityList.put(embedPath, it.asClassName())

        it.enclosedElements.filter { it.getAnnotation(Embedded::class.java) != null}.forEach {
            val subEmbedPath = mutableListOf(*embedPath.toTypedArray()) + "${it.simpleName}"
            val subClassName = it.asType().asTypeName() as ClassName
//            syncableEntityList.putAll(findEntitiesWithAnnotation(it.asType().asTypeName() as ClassName,
//                    annotationClass, processingEnv, subEmbedPath))
            syncableEntityList.putAll(subClassName.findAllEntitiesWithAnnotation(annotationClass,
                    processingEnv, subEmbedPath))
        }
    }

    return syncableEntityList.toMap()
}

/**
 * Replaces AbstractDbProcessor.findSyncableEntities
 *
 * Get a list of all the syncable entities associated with a given POJO. This will look at parent
 * classes and embedded fields
 *
 * @param entityType the POJO to inspect to find syncable entities. This will inspect the class
 * itself, the parent classes, and any fields annotated with Embedded
 * @param processingEnv the annotation processor environment
 * @param embedPath the current embed path. This function is designed to work recursively.
 *
 * @return A map in the form of a list of the embedded variables to the syncable entity
 * e.g.
 * given
 *
 * <pre>
 * class SyncableEntityWithOtherSyncableEntity(@Embedded var embedded: OtherSyncableEntity?): SyncableEntity()
 * </pre>
 * This will result in:
 * <pre>
 * {
 * [] -> SyncableEntity,
 * ['embedded'] -> OtherSyncableEntity
 * }
 * </pre>
 */
fun ClassName.findAllSyncableEntities(processingEnv: ProcessingEnvironment,
                         embedPath: List<String> = listOf()) =
        findAllEntitiesWithAnnotation(SyncableEntity::class.java, processingEnv, embedPath)

fun ClassName.entitySyncableTypes(processingEnv: ProcessingEnvironment): List<ClassName> {
    return findAllSyncableEntities(processingEnv).values.toList()
}

/**
 * Shorthand to check if this entity (or any of it's parents or embedded entities) contain
 * syncable entities
 */
fun ClassName.entityHasSyncableEntityTypes(processingEnv: ProcessingEnvironment) : Boolean
    = findAllSyncableEntities(processingEnv).isNotEmpty()


/**
 * Convenience shorthand for creating a new classname with the given suffix and the same package
 * as the original
 */
fun ClassName.withSuffix(suffix: String) = ClassName(this.packageName, "$simpleName$suffix")
