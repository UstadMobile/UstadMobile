package db2

import androidx.room.Database
import com.ustadmobile.door.*
import com.ustadmobile.door.annotation.MinSyncVersion
import com.ustadmobile.door.entities.ChangeLog
import db2.ExampleDatabase2.Companion.DB_VERSION

@Database(version = DB_VERSION, entities = [ExampleEntity2::class, ExampleLinkEntity::class,
    ExampleEntityPkInt::class, DoorDatabaseSyncInfo::class,
    SyncNode::class,
    SyncResult::class,
    ExampleSyncableEntity::class,
    OtherSyncableEntity::class,
    ExampleAttachmentEntity::class,
    ChangeLog::class
    //#DOORDB_TRACKER_ENTITIES

])
@MinSyncVersion(1)
abstract class ExampleDatabase2 : DoorDatabase(), SyncableDoorDatabase {

    abstract fun exampleSyncableDao(): ExampleSyncableDao

    abstract fun exampleDao2(): ExampleDao2

    abstract fun exampleLinkedEntityDao(): ExampleLinkEntityDao

    abstract fun examlpeDaoWithInterface(): ExampleDaoWithInterface

    abstract fun exampleEntityPkIntDao(): ExampleEntityPkIntDao

    abstract fun exampleAttachmentDao(): ExampleAttachmentDao

    //#DOORDB_SYNCDAO


    companion object {
        const val DB_VERSION = 2
    }
}