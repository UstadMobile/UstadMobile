package com.ustadmobile.lib.database.annotation

import kotlin.reflect.KClass

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class UmDatabase(val entities: Array<KClass<*>>, val version: Int)
