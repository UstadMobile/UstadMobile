package com.ustadmobile.door

import android.content.Context
import androidx.room.Room
import kotlin.reflect.KClass
import androidx.room.RoomDatabase

actual class DatabaseBuilder<T: DoorDatabase>(private val roomBuilder: RoomDatabase.Builder<T>) {

    actual companion object {
        actual fun <T : DoorDatabase> databaseBuilder(context: Any, dbClass: KClass<T>, dbName: String): DatabaseBuilder<T> {
            return DatabaseBuilder(Room.databaseBuilder(context as Context, dbClass.java, dbName))
        }
    }

    actual fun build(): T = roomBuilder.build()

}