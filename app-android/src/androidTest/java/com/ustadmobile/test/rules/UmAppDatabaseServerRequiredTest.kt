package com.ustadmobile.test.rules


/**
 * Indicates that the given test function requires a real server to be operational. This should be
 * used in conjunction with the UmAppDatabaseAndroidClientRule
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class UmAppDatabaseServerRequiredTest