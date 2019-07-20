package com.ustadmobile.door.annotation

import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)

/**
 * Annotation to mark an entity as Syncable. This will
 *
 * 1. Modify the primary key generation to ensure that primary keys don't collide
 * 2. Require that the entity contains a local and master change sequence number.
 * 3. Require that a syncTrackerEntity is specified. This will be used to track the delivery of
 * changes. It must have annotation for the Tracker* annotations
 */
annotation class SyncableEntity(val tableId: Int, val syncTrackerEntity: KClass<*>)
