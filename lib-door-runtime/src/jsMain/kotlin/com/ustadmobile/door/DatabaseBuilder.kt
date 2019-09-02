package com.ustadmobile.door

import io.ktor.client.HttpClient
import org.w3c.dom.get
import kotlin.browser.localStorage
import kotlin.reflect.KClass

actual class DatabaseBuilder<T: DoorDatabase>(private var context: Any, private var dbClass: KClass<T>, private var dbName: String){

    actual companion object {
        actual fun <T : DoorDatabase> databaseBuilder(context: Any, dbClass: KClass<T>, dbName: String): DatabaseBuilder<T>
                = DatabaseBuilder(context, dbClass, dbName)
    }

    actual fun build(): T {
        val dbEndpoint = localStorage["doordb.endpoint.url"]
        return dbClass.js.createInstance(HttpClient(), dbEndpoint, dbName)
    }

    actual fun addCallback(callback: DoorDatabaseCallback): DatabaseBuilder<T> {
        //do nothing

        return this
    }

    actual fun addMigrations(vararg migrations: DoorMigration): DatabaseBuilder<T> {
        //do nothing

        return this
    }

}