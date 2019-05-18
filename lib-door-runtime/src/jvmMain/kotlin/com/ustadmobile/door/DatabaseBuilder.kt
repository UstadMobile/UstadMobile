package com.ustadmobile.door

import javax.naming.InitialContext
import javax.sql.DataSource
import kotlin.reflect.KClass

actual class DatabaseBuilder<T: DoorDatabase>(private var context: Any, private var dbClass: KClass<T>, private var dbName: String){


    actual companion object {
        actual fun <T : DoorDatabase> databaseBuilder(context: Any, dbClass: KClass<T>, dbName: String): DatabaseBuilder<T>
            = DatabaseBuilder(context, dbClass, dbName)
    }

    actual fun build(): T {
        val iContext = InitialContext()
        val dataSource = iContext.lookup("java:/comp/env/jdbc/${dbName}") as DataSource
        val dbImplClass = Class.forName("${dbClass.java.canonicalName}_JdbcKt") as Class<T>
        val doorDb = dbImplClass.getConstructor(DataSource::class.java).newInstance(dataSource)

        doorDb.createAllTables()

        return doorDb
    }

}