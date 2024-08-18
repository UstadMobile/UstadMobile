package com.ustadmobile.centraldb

import com.ustadmobile.centraldb.daos.LearningSpaceConfigDao
import com.ustadmobile.centraldb.daos.LearningSpaceInfoDao
import com.ustadmobile.centraldb.entities.LearningSpaceInfo
import com.ustadmobile.door.annotation.DoorDatabase
import com.ustadmobile.door.room.RoomDatabase

@DoorDatabase(
    entities = [LearningSpaceInfo::class],
    version = 1,
)
expect abstract class CentralDb : RoomDatabase {

    abstract fun learningSpaceInfoDao(): LearningSpaceInfoDao

    abstract fun learningSpaceConfigDao(): LearningSpaceConfigDao

}