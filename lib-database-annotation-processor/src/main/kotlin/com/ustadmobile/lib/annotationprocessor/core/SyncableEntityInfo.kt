package com.ustadmobile.lib.annotationprocessor.core

import androidx.room.PrimaryKey
import com.squareup.kotlinpoet.*
import com.ustadmobile.door.annotation.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

class SyncableEntityInfo {

    lateinit var syncableEntity: ClassName

    lateinit var entityPkField: PropertySpec

    lateinit var entityMasterCsnField: PropertySpec

    lateinit var entityLocalCsnField: PropertySpec

    lateinit var entityLastChangedByField: PropertySpec

    lateinit var tracker: ClassName

    lateinit var trackerCsnField: PropertySpec

    lateinit var trackerPkField: PropertySpec

    lateinit var trackerDestField: PropertySpec

    lateinit var trackerReceivedField: PropertySpec

    lateinit var trackerReqIdField: PropertySpec

    var tableId: Int = 0

    constructor(syncableEntityParam: ClassName, processingEnv: ProcessingEnvironment) {
        syncableEntity = syncableEntityParam
        val syncableEntityEl = processingEnv.elementUtils.getTypeElement(syncableEntity.canonicalName)
        tableId = syncableEntityEl.getAnnotation(SyncableEntity::class.java).tableId
        val entityPkFieldEl = syncableEntityEl.enclosedElements
                .first { it.getAnnotation(PrimaryKey::class.java) != null }
        entityPkField = PropertySpec.builder("${entityPkFieldEl.simpleName}",
                entityPkFieldEl.asType().asTypeName()).build()

        val entityMasterCsnFieldEl = syncableEntityEl.enclosedElements
                .first { it.getAnnotation(MasterChangeSeqNum::class.java) != null}
        entityMasterCsnField = PropertySpec.builder("${entityMasterCsnFieldEl.simpleName}",
                entityMasterCsnFieldEl.asType().asTypeName()).build()

        val entityLocalCsnFieldEl = syncableEntityEl.enclosedElements
                .first { it.getAnnotation(LocalChangeSeqNum::class.java) != null}
        entityLocalCsnField = PropertySpec.builder("${entityLocalCsnFieldEl.simpleName}",
                entityLocalCsnFieldEl.asType().asTypeName()).build()


        val entityLastModifiedField = syncableEntityEl.enclosedElements
                .first { it.getAnnotation(LastChangedBy::class.java) != null}
        entityLastChangedByField = PropertySpec.builder("${entityLastModifiedField.simpleName}",
                entityLastModifiedField.asType().asTypeName()).build()


        tracker = ClassName(syncableEntityParam.packageName,
                "${syncableEntityParam.simpleName}${DbProcessorSync.TRACKER_SUFFIX}")

        trackerCsnField = PropertySpec.builder(DbProcessorSync.TRACKER_CHANGESEQNUM_FIELDNAME,
                LONG).build()

        trackerPkField = PropertySpec.builder(DbProcessorSync.TRACKER_ENTITY_PK_FIELDNAME,
                entityPkField.type).build()

        trackerDestField = PropertySpec.builder(DbProcessorSync.TRACKER_DESTID_FIELDNAME,
                INT).build()

        trackerReceivedField = PropertySpec.builder(DbProcessorSync.TRACKER_RECEIVED_FIELDNAME,
                BOOLEAN).build()

        trackerReqIdField = PropertySpec.builder(DbProcessorSync.TRACKER_REQUESTID_FIELDNAME,
                INT).build()
    }




}

