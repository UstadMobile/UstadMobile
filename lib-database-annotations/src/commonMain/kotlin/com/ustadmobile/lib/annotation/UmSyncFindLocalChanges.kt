package com.ustadmobile.lib.database.annotation

/**
 * Used by the UmSyncOutgoing method to find local changes using the local change sequence number
 * (only).
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.BINARY)
annotation class UmSyncFindLocalChanges
