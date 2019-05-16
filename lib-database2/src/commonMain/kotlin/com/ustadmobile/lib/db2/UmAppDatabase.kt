package com.ustadmobile.lib.db2

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ustadmobile.lib.db2.dao.TestDao


@Database(entities = [TestEntity::class], version = 1)
abstract class UmAppDatabase : RoomDatabase() {

    abstract fun testDao(): TestDao

}