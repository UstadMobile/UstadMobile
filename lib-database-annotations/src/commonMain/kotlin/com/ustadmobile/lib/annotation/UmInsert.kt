package com.ustadmobile.lib.database.annotation

/**
 * Created by mike on 1/15/18.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class UmInsert(val onConflict: Int = UmOnConflictStrategy.ABORT, val preserveLastChangedBy: Boolean = false)
