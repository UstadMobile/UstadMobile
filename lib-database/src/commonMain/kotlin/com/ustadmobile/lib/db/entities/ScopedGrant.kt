package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ScopedGrant.Companion.TABLE_ID
import kotlinx.serialization.Serializable

/**
 * Represents permission granted to a given PersonGroup scoped to a specific entity.
 */
@Entity(indices = [
    Index(value = ["sgGroupUid", "sgPermissions", "sgTableId", "sgEntityUid"], name = "idx_group_to_entity"),
    Index(value = ["sgTableId", "sgEntityUid", "sgPermissions", "sgGroupUid"], name = "idx_entity_to_group")]
)

@ReplicateEntity(
    tableId = TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(arrayOf(
    Trigger(name = "sg_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
        sqlStatements = ["UPDATE ScopedGrant SET sgLct = 0 WHERE sgUid = 0"],
    )
))
@Serializable
open class ScopedGrant {

    @PrimaryKey(autoGenerate = true)
    var sgUid: Long = 0

    @MasterChangeSeqNum
    var sgPcsn: Long = 0

    @LocalChangeSeqNum
    var sgLcsn: Long = 0

    @LastChangedBy
    var sgLcb: Int = 0

    @ReplicateEtag
    @ReplicateLastModified
    var sgLct: Long = 0

    //The table id that this grant is form, or ALL_TABLES to indicate it is for all tables (eg. superadmin)
    var sgTableId: Int = 0

    //The entity uid that this grant is for, or ALL_ENTITIES to indicate it is for all entities (e.g. superadmin)
    var sgEntityUid: Long = 0

    //Actual scoped permissions granted (bitmask) as per Role.PERMISSION_ constants
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


    }
}