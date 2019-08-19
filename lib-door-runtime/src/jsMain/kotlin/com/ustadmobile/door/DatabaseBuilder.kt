package com.ustadmobile.door

import kotlin.reflect.KClass

actual class DatabaseBuilder<T: DoorDatabase>(private var context: Any, private var dbClass: KClass<T>, private var dbName: String){


    actual companion object {
        actual fun <T : DoorDatabase> databaseBuilder(context: Any, dbClass: KClass<T>, dbName: String): DatabaseBuilder<T>
                = DatabaseBuilder(context, dbClass, dbName)
    }

    actual fun build(): T {
        throw Exception("Not implemented on JS yet")
    }

    actual fun addCallback(callback: DoorDatabaseCallback){
        //do nothing
    }

}