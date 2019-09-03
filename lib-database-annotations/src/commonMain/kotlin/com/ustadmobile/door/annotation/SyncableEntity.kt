package com.ustadmobile.door.annotation

import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)

/**
 * Annotation to mark an entity as Syncable. This will
 *
 * 1. Modify the primary key generation to ensure that primary keys don't collide
 * 2. Require that the entity contains a local and master change sequence number.
 */
annotation class SyncableEntity(val tableId: Int)
