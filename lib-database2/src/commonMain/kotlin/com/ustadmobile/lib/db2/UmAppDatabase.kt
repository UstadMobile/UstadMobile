package com.ustadmobile.lib.db2

import androidx.room.Database
import com.ustadmobile.door.DoorDatabase
import com.ustadmobile.lib.db2.dao.TestDao


@Database(entities = [TestEntity::class], version = 1)
abstract class UmAppDatabase : DoorDatabase() {

    abstract fun testDao(): TestDao

}