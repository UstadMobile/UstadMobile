package com.ustadmobile.lib.database.annotation


@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class UmUpdate(val preserveLastChangedBy: Boolean = false)
