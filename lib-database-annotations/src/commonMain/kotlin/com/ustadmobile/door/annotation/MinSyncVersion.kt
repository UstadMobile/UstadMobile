package com.ustadmobile.door.annotation

/**
 * Annotation that indicates the minimum version that will be accepted for an incoming sync client.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class MinSyncVersion(val value: Int)
