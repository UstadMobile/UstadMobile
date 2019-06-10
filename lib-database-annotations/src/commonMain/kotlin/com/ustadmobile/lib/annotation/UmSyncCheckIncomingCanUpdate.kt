package com.ustadmobile.lib.database.annotation

/**
 * Used by the UmSyncIncoming annotated method to determine which entities, from a given list of
 * entities or primary keys, exist, and out of those, which of them can be updated by a given user
 * account.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.BINARY)
annotation class UmSyncCheckIncomingCanUpdate
