package db2

import androidx.room.Database
import com.ustadmobile.door.DoorDatabase
import com.ustadmobile.door.DoorDatabaseSyncInfo
import com.ustadmobile.door.SyncableDoorDatabase

@Database(version = 1, entities = [ExampleEntity2::class, ExampleLinkEntity::class,
    ExampleEntityPkInt::class, DoorDatabaseSyncInfo::class,
    ExampleSyncableEntity::class, ExampleSyncableEntityTracker::class,
    OtherSyncableEntity::class, OtherSyncableEntityTracker::class])
abstract class ExampleDatabase2 : DoorDatabase(), SyncableDoorDatabase {

    override val nodeId
        get() = 0

    abstract fun exampleSyncableDao(): ExampleSyncableDao

    abstract fun exampleDao2(): ExampleDao2

    abstract fun exampleLinkedEntityDao(): ExampleLinkEntityDao

    abstract fun examlpeDaoWithInterface(): ExampleDaoWithInterface

    abstract fun exampleEntityPkIntDao(): ExampleEntityPkIntDao



}