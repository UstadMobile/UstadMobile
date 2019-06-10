package com.ustadmobile.lib.database.annotation

/**
 * Annotation indicates that this method should count the number of pending local changes that have
 * not yet been synced (used to determine if a task needs to be scheduled on the device to sync when
 * connectivity is regained).
 *
 * On a DAO: this should generate a count of the number of pending local changes made on this table.
 *
 * On a database: this should generate a count of the number of pending changes on all syncable DAOs
 * in this database.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class UmSyncCountLocalPendingChanges
