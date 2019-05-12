package com.ustadmobile.lib.database.annotation

/**
 * Created by mike on 1/21/18.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
/*
 Kotlin translation: removed [] as this does not translate to javascript
 */
annotation class UmEntity(/*val primaryKeys: Array<String> = [], val indices: Array<UmIndex> = [], */val tableId: Int = 0)
