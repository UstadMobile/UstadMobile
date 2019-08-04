package com.ustadmobile.door

import java.util.*
import javax.naming.InitialContext
import javax.naming.NamingException
import javax.sql.DataSource
import kotlin.reflect.KClass


actual class DatabaseBuilder<T: DoorDatabase>(private var context: Any, private var dbClass: KClass<T>, private var dbName: String){

    private val callbacks = mutableListOf<DoorDatabaseCallback>()

    actual companion object {
        actual fun <T : DoorDatabase> databaseBuilder(context: Any, dbClass: KClass<T>, dbName: String): DatabaseBuilder<T>
            = DatabaseBuilder(context, dbClass, dbName)
    }

    @Suppress("UNCHECKED_CAST")
    actual fun build(): T {
        val iContext = InitialContext()
        val dataSource = iContext.lookup("java:/comp/env/jdbc/${dbName}") as DataSource
        val dbImplClass = Class.forName("${dbClass.java.canonicalName}_JdbcKt") as Class<T>

        val doorDb = if(SyncableDoorDatabase::class.java.isAssignableFrom(dbImplClass)) {
            var isMaster = false
            try {
                val isMasterObj = iContext.lookup("java:/comp/env/doordb/$dbName/master")
                isMaster = if(isMasterObj != null && isMasterObj is Boolean) { isMasterObj } else { false }
            }catch(namingException: NamingException) {
                System.err.println("Warning: could not check if $dbName is master or not, assuming false")
            }

            dbImplClass.getConstructor(DataSource::class.java, Boolean::class.javaPrimitiveType)
                    .newInstance(dataSource, isMaster)
        }else {
            dbImplClass.getConstructor(DataSource::class.java).newInstance(dataSource)
        }


        if(!doorDb.tableNames.any {it.toLowerCase(Locale.ROOT) == DoorDatabase.DBINFO_TABLENAME}) {
            doorDb.createAllTables()
            callbacks.forEach { it.onCreate(doorDb.sqlDatabaseImpl) }
        }

        callbacks.forEach { it.onOpen(doorDb.sqlDatabaseImpl)}
        return doorDb
    }

    actual fun addCallback(callback: DoorDatabaseCallback) {
        callbacks.add(callback)
    }

}