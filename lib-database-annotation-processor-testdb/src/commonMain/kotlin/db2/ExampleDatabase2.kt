package db2

import androidx.room.Database
import com.ustadmobile.door.*

@Database(version = 1, entities = [ExampleEntity2::class, ExampleLinkEntity::class,
    ExampleEntityPkInt::class, DoorDatabaseSyncInfo::class,
    SyncNode::class,
    SyncResult::class,
    ExampleSyncableEntity::class,
    OtherSyncableEntity::class,
    ExampleAttachmentEntity::class
    //#DOORDB_TRACKER_ENTITIES

])
abstract class ExampleDatabase2 : DoorDatabase(), SyncableDoorDatabase {

    abstract fun exampleSyncableDao(): ExampleSyncableDao

    abstract fun exampleDao2(): ExampleDao2

    abstract fun exampleLinkedEntityDao(): ExampleLinkEntityDao

    abstract fun examlpeDaoWithInterface(): ExampleDaoWithInterface

    abstract fun exampleEntityPkIntDao(): ExampleEntityPkIntDao

    abstract fun exampleAttachmentDao(): ExampleAttachmentDao

    //#DOORDB_SYNCDAO

}