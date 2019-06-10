package com.ustadmobile.lib.database.annotation

/**
 * Indicates that this method is the one to be used by sync methods to find all changes, searching
 * by local and master change sequence number ranges. This is used by the UmSyncIncoming method to
 * find all changes to send back.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.BINARY)
annotation class UmSyncFindAllChanges
