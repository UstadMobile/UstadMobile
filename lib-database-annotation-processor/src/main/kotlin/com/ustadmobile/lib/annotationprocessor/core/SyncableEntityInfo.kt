package com.ustadmobile.lib.annotationprocessor.core

import androidx.room.PrimaryKey
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
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

    constructor(syncableEntityParam: ClassName, processingEnv: ProcessingEnvironment) {
        syncableEntity = syncableEntityParam
        val syncableEntityEl = processingEnv.elementUtils.getTypeElement(syncableEntity.canonicalName)
        val entityPkFieldEl = syncableEntityEl.enclosedElements
                .first { it.getAnnotation(PrimaryKey::class.java) != null }
        entityPkField = PropertySpec.builder("${entityPkFieldEl.simpleName}",
                entityPkFieldEl.asType().asTypeName()).build()

        val entityMasterCsnFieldEl = syncableEntityEl.enclosedElements
                .first { it.getAnnotation(MasterChangeSeqNum::class.java) != null}
        entityMasterCsnField = PropertySpec.builder("${entityMasterCsnFieldEl.simpleName}",
                entityMasterCsnFieldEl.asType().asTypeName()).build()

        val entityLocalCsnFieldEl = syncableEntityEl.enclosedElements
                .first { it.getAnnotation(MasterChangeSeqNum::class.java) != null}
        entityLocalCsnField = PropertySpec.builder("${entityLocalCsnFieldEl.simpleName}",
                entityLocalCsnFieldEl.asType().asTypeName()).build()


        val entityLastModifiedField = syncableEntityEl.enclosedElements
                .first { it.getAnnotation(LastChangedBy::class.java) != null}
        entityLastChangedByField = PropertySpec.builder("${entityLastModifiedField.simpleName}",
                entityLastModifiedField.asType().asTypeName()).build()


        val syncableEntityTracker = getEntitySyncTracker(syncableEntityEl, processingEnv)
        val syncableEntityTrackerEl = processingEnv.typeUtils.asElement(syncableEntityTracker) as TypeElement
        tracker = syncableEntityTrackerEl.asClassName()

        val trackerCsnFieldEl = syncableEntityTrackerEl.enclosedElements
                .first { it.getAnnotation(TrackerChangeSeqNum::class.java) != null }
        trackerCsnField = PropertySpec.builder("${trackerCsnFieldEl.simpleName}",
                trackerCsnFieldEl.asType().asTypeName()).build()

        val trackerPkFieldEl = syncableEntityTrackerEl.enclosedElements
                .first {it.getAnnotation(TrackerEntityPrimaryKey::class.java) != null}
        trackerPkField = PropertySpec.builder("${trackerPkFieldEl.simpleName}",
                trackerPkFieldEl.asType().asTypeName()).build()

        val trackerDestFieldEl = syncableEntityTrackerEl.enclosedElements
                .first {it.getAnnotation(TrackDestId::class.java) != null}
        trackerDestField = PropertySpec.builder("${trackerDestFieldEl.simpleName}",
                trackerDestFieldEl.asType().asTypeName()).build()

        val trackerReceivedFieldEl = syncableEntityTrackerEl.enclosedElements
                .first {it.getAnnotation(TrackerReceived::class.java) != null}
        trackerReceivedField = PropertySpec.builder("${trackerReceivedFieldEl.simpleName}",
                trackerReceivedFieldEl.asType().asTypeName()).build()

        val trackerReqIdFieldEl = syncableEntityTrackerEl.enclosedElements
                .first {it.getAnnotation(TrackerRequestId::class.java) != null}
        trackerReqIdField = PropertySpec.builder("${trackerReqIdFieldEl.simpleName}",
                trackerReqIdFieldEl.asType().asTypeName()).build()
    }




}

