package com.ustadmobile.lib.database.annotation

/**
 * Indicates that the annotated method should be generated to send a sync to the corresponding
 * UmSyncIncoming method for this DAO. Parameters as follows:
 * D - it's own DAO type
 * accountPersonUid - personUid for the the account being used to run the sync
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.BINARY)
annotation class UmSyncOutgoing
