package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

/**
 * Represents a group in the system. Each individual user also has their own one member group.
 */
@Triggers(arrayOf(
     Trigger(
         name = "persongroup_remote_insert",
         order = Trigger.Order.INSTEAD_OF,
         on = Trigger.On.RECEIVEVIEW,
         events = [Trigger.Event.INSERT],
         sqlStatements = [
            TRIGGER_UPSERT_WHERE_NEWER
         ]
     )
))
@Entity
@Serializable
@ReplicateEntity(
    tableId = PersonGroup.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
open class PersonGroup() {

    @PrimaryKey(autoGenerate = true)
    var groupUid: Long = 0

    @MasterChangeSeqNum
    var groupMasterCsn: Long = 0

    @LocalChangeSeqNum
    var groupLocalCsn: Long = 0

    @LastChangedBy
    var groupLastChangedBy: Int = 0

    @ReplicateLastModified
    @ReplicateEtag
    var groupLct: Long = 0

    var groupName: String? = null

    var groupActive : Boolean = true

    /**
     *
     */
    var personGroupFlag: Int = 0

    constructor(name: String) : this() {
        this.groupName = name
    }

    companion object{

        const val TABLE_ID = 43

        const val PERSONGROUP_FLAG_DEFAULT = 0

        const val PERSONGROUP_FLAG_PERSONGROUP = 1

        const val PERSONGROUP_FLAG_PARENT_GROUP = 2

        const val PERSONGROUP_FLAG_STUDENTGROUP = 4

        const val PERSONGROUP_FLAG_TEACHERGROUP = 8

        const val PERSONGROUP_FLAG_GUESTPERSON = 16


    }
}
