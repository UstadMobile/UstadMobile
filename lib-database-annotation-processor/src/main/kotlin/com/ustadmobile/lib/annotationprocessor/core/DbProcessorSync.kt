package com.ustadmobile.lib.annotationprocessor.core

import androidx.room.Dao
import androidx.room.Database
import androidx.room.PrimaryKey
import androidx.room.Query
import com.squareup.kotlinpoet.*
import com.ustadmobile.door.annotation.*
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

fun getEntitySyncTracker(entityEl: Element, processingEnv: ProcessingEnvironment): TypeMirror? {
    val syncEntityAnnotationIndex = entityEl.annotationMirrors.map {processingEnv.typeUtils.asElement(it.annotationType) as TypeElement }
            .indexOfFirst { it.qualifiedName.toString() == "com.ustadmobile.door.annotation.SyncableEntity" }
    if(syncEntityAnnotationIndex == -1)
        return null

    val annotationValue = entityEl.annotationMirrors[syncEntityAnnotationIndex].elementValues
            .filter { it.key.simpleName.toString() == "syncTrackerEntity" }.values.toList()[0]
    return annotationValue.value as TypeMirror
}

class DbProcessorSync: AbstractDbProcessor() {

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        setupDb(roundEnv)

        val outputArg = processingEnv.options[OPTION_OUTPUT_DIR]
        val outputDir = if(outputArg == null || outputArg == "filer") processingEnv.options["kapt.kotlin.generated"]!! else outputArg
        val dbs = roundEnv.getElementsAnnotatedWith(Database::class.java)

        for(dbTypeEl in dbs) {
            val dbFileSpec = generateSyncDaoInterfaceAndImpl(dbTypeEl as TypeElement)
            dbFileSpec.writeTo(File(outputDir))
        }

        return true
    }

    fun generateSyncDaoInterfaceAndImpl(dbType: TypeElement): FileSpec {
        val syncInterfaceSimpleName = "${dbType.simpleName}$SUFFIX_SYNCDAO_ABSTRACT"
        val syncInterfaceClassName = ClassName(pkgNameOfElement(dbType, processingEnv),
                syncInterfaceSimpleName)
        val syncDaoInterface = TypeSpec.interfaceBuilder(syncInterfaceSimpleName)
        val syncDaoImpl = TypeSpec.classBuilder("${dbType.simpleName}$SUFFIX_SYNCDAO_IMPL")
                .addSuperinterface(syncInterfaceClassName)
                .addAnnotation(Dao::class.asClassName())
        val abstractFileSpec = FileSpec.builder(pkgNameOfElement(dbType, processingEnv),
                "${dbType.simpleName}$SUFFIX_SYNCDAO_ABSTRACT")

        entityTypesOnDb(dbType, processingEnv).filter { it.getAnnotation(SyncableEntity::class.java) != null}.forEach {entityType ->
            //interface and impl funspecs
            val localUnsentChangeFuns = (0..1).map { FunSpec.builder("_find${entityType.simpleName}LocalUnsentChanges") }
            localUnsentChangeFuns.forEach {
                it.addParameter("destClientId", INT)
                        .addParameter("limit", INT)
            }

            val entityPkField = entityType.enclosedElements.first { it.getAnnotation(PrimaryKey::class.java) != null }
            val entitySyncTracker = getEntitySyncTracker(entityType, processingEnv)
            val entitySyncTrackerEl = processingEnv.typeUtils.asElement(entitySyncTracker)
            val entityLocalCsnFieldEl = entityType.enclosedElements
                    .first { it.getAnnotation(LocalChangeSeqNum::class.java) != null}
            val entitySyncTrackCsnField = entitySyncTrackerEl.enclosedElements
                    .first { it.getAnnotation(TrackerChangeSeqNum::class.java) != null }
            val entitySyncTrackerPkField = entitySyncTrackerEl.enclosedElements
                    .first {it.getAnnotation(TrackerEntityPrimaryKey::class.java) != null}
            val entitySyncTrackerDestField = entitySyncTrackerEl.enclosedElements
                    .first {it.getAnnotation(TrackDestId::class.java) != null}

            val findLocalUnsentSql = "SELECT * FROM " +
                    "(SELECT * FROM ${entityType.simpleName} ) AS ${entityType.simpleName} " +
                    "WHERE " +
                    "(${entityType.simpleName}.$entityLocalCsnFieldEl > " +
                    "COALESCE((SELECT ${entitySyncTrackCsnField.simpleName} FROM {${entitySyncTrackerEl.simpleName} " +
                    "WHERE ${entitySyncTrackerPkField.simpleName} = ${entityType.simpleName}.${entityPkField.simpleName} " +
                    "AND ${entitySyncTrackerDestField.simpleName} = :destClientId)," +
                    "0) LIMIT :limit"
            localUnsentChangeFuns[0].addAnnotation(AnnotationSpec.builder(Query::class)
                    .addMember("value = %S", findLocalUnsentSql).build())
            syncDaoInterface.addFunction(localUnsentChangeFuns[0].build())
        }


        abstractFileSpec.addType(syncDaoInterface.build())
        return abstractFileSpec.build()
    }


    companion object {

        const val OPTION_OUTPUT_DIR = "door_sync_output"

        const val SUFFIX_SYNCDAO_ABSTRACT = "SyncDao"

        const val SUFFIX_SYNCDAO_IMPL = "SyncDaoImpl"


    }

}