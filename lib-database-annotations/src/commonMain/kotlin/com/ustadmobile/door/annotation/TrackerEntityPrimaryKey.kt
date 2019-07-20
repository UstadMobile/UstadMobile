package com.ustadmobile.door.annotation

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FIELD)

/**
 * Field on the tracker entity that is the foreign key, linking to the primary key on the
 * syncable entity itself
 */
annotation class TrackerEntityPrimaryKey