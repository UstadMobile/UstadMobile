package com.ustadmobile.lib.database.annotation

/**
 * Created by mike on 1/21/18.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
annotation class UmPrimaryKey(val autoIncrement: Boolean = false, val autoGenerateSyncable: Boolean = false)
