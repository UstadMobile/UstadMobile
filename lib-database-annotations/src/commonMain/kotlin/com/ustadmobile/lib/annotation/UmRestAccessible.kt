package com.ustadmobile.lib.database.annotation

/**
 * Used to indicate that the given method should be accessible over HTTP REST
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class UmRestAccessible(val timeout: Int = 5000)
