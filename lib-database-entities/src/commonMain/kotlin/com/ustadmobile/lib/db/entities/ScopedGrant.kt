package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ScopedGrant.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity(indices = [
    Index(value = ["sgGroupUid", "sgPermissions", "sgTableId", "sgEntityUid"], name = "idx_group_to_entity"),
    Index(value = ["sgTableId", "sgEntityUid", "sgPermissions", "sgGroupUid"], name = "idx_entity_to_group")]
)

@SyncableEntity(tableId = TABLE_ID)
@Serializable
class ScopedGrant {

    @PrimaryKey(autoGenerate = true)
    var sgUid: Long = 0

    @MasterChangeSeqNum
    var sgPcsn: Long = 0

    @LocalChangeSeqNum
    var sgLcsn: Long = 0

    @LastChangedBy
    var sgLcb: Int = 0

    @LastChangedTime
    var sgLct: Long = 0

    //The table id that this grant is form, or ALL_TABLES to indicate it is for all tables (eg. superadmin)
    var sgTableId: Int = 0

    //The entity uid that this grant is for, or ALL_ENTITIES to indicate it is for all entities (e.g. superadmin)
    var sgEntityUid: Long = 0

    //Actual scoped permissions granted (bitmask)
    var sgPermissions: Long = 0

    //The group that these permissions are granted to
    var sgGroupUid: Long = 0

    //an index that will determine the order in which it is displayed
    var sgIndex: Int = 0

    var sgFlags: Int = 0

    companion object {
        const val TABLE_ID = 48

        /**
         *
         */
        const val ALL_TABLES = -2


        const val ALL_ENTITIES = -2L

        const val FLAG_NO_DELETE = 1

        const val FLAG_NO_EDIT = 2

        const val FLAG_ADMIN_GROUP = 4

        const val FLAG_TEACHER_GROUP = 8

        const val FLAG_STUDENT_GROUP = 16

        const val FLAG_PARENT_GRANT = 32

    }
}