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
annotation class SyncableEntity(
        /**
         * The table id must be a unique integer that is not used by any other table on the database
         */
        val tableId: Int,

        /**
         * If not-blank, this query should list all the device ids that must should receive a
         * notification when this entity is inserted or updated. This is done by the primary node
         */
        val pushNotifyOnUpdate: String = "",


        val syncFindAllQuery: String = "")
