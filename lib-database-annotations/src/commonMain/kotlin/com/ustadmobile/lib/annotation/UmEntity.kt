package com.ustadmobile.lib.database.annotation

/**
 * Created by mike on 1/21/18.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)

annotation class UmEntity(val primaryKeys: Array<String> = arrayOf(), val indices: Array<UmIndex> = arrayOf(), val tableId: Int = 0)
