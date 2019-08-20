package db2

import androidx.room.Database
import com.ustadmobile.door.DoorDatabase
import com.ustadmobile.door.DoorDatabaseSyncInfo
import com.ustadmobile.door.SyncableDoorDatabase
import com.ustadmobile.door.SyncNode

@Database(version = 1, entities = [ExampleEntity2::class, ExampleLinkEntity::class,
    ExampleEntityPkInt::class, DoorDatabaseSyncInfo::class,
    SyncNode::class,
    ExampleSyncableEntity::class,
    OtherSyncableEntity::class
    //#DOORDB_TRACKER_ENTITIES

])
abstract class ExampleDatabase2 : DoorDatabase(), SyncableDoorDatabase {

    abstract fun exampleSyncableDao(): ExampleSyncableDao

    abstract fun exampleDao2(): ExampleDao2

    abstract fun exampleLinkedEntityDao(): ExampleLinkEntityDao

    abstract fun examlpeDaoWithInterface(): ExampleDaoWithInterface

    abstract fun exampleEntityPkIntDao(): ExampleEntityPkIntDao

    //#DOORDB_SYNCDAO

}