package com.ustadmobile.door

import io.ktor.client.features.json.serializer.KotlinxSerializer
import org.w3c.dom.get
import kotlin.browser.localStorage
import kotlin.reflect.KClass

actual class DatabaseBuilder<T: DoorDatabase>(private var context: Any, private var dbClass: KClass<T>, private var dbName: String){

    actual companion object {
        actual fun <T : DoorDatabase> databaseBuilder(context: Any, dbClass: KClass<T>, dbName: String): DatabaseBuilder<T>
                = DatabaseBuilder(context, dbClass, dbName)

        private val DB_TO_JS_IMPL_MAP = mutableMapOf<KClass<*>, KClass<*>>()

        fun registerImpl(dbClass: KClass<*>, implClass: KClass<*>) {
            DB_TO_JS_IMPL_MAP.put(dbClass, implClass)
        }
    }

    actual fun build(): T {
        val jsImplClass = DB_TO_JS_IMPL_MAP[dbClass]
        if(jsImplClass == null) {
            throw IllegalStateException("Could not find implementation of $dbName. " +
                    "Please make sure to load the DbName_JsImpl")
        }

        println(KotlinxSerializer::class)
        val dbEndpoint = localStorage["doordb.endpoint.url"]
        if(dbEndpoint == null) {
            throw IllegalStateException("Door Database: doordb endpoint url not set. Please set " +
                    "the local storage property doordb.endpoint.url")
        }

        console.log("DoorDbJs endpoint = $dbEndpoint")
        @Suppress("UNUSED_VARIABLE")

        return jsImplClass.js.createInstance(dbEndpoint, dbName).unsafeCast<T>()
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