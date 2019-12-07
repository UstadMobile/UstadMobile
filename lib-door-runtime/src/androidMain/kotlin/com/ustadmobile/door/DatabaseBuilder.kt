package com.ustadmobile.door

import android.content.Context
import androidx.room.Room
import kotlin.reflect.KClass
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

actual class DatabaseBuilder<T: DoorDatabase>(private val roomBuilder: RoomDatabase.Builder<T>) {

    actual companion object {
        actual fun <T : DoorDatabase> databaseBuilder(context: Any, dbClass: KClass<T>, dbName: String): DatabaseBuilder<T> {
            val builder = DatabaseBuilder(Room.databaseBuilder(context as Context, dbClass.java, dbName)
                    .fallbackToDestructiveMigration())

            val callbackClassName = "${dbClass.java.canonicalName}_SyncCallback"
            println("Attempt to load callback $callbackClassName")

            val callbackClass = Class.forName(callbackClassName).newInstance() as DoorDatabaseCallback

            builder.addCallback(callbackClass)

            return builder
        }
    }



    actual fun build(): T = roomBuilder.allowMainThreadQueries().build()

    actual fun addCallback(callback: DoorDatabaseCallback) : DatabaseBuilder<T> {
        roomBuilder.addCallback(object: RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase)  = callback.onCreate(db)

            override fun onOpen(db: SupportSQLiteDatabase) = callback.onOpen(db)
        })

        return this
    }

    actual fun addMigrations(vararg migrations: DoorMigration): DatabaseBuilder<T> {
        roomBuilder.addMigrations(*migrations)
        return this
    }


}