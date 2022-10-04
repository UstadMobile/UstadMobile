package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Triggers(arrayOf(
     Trigger(
         name = "persongroup_remote_insert",
         order = Trigger.Order.INSTEAD_OF,
         on = Trigger.On.RECEIVEVIEW,
         events = [Trigger.Event.INSERT],
         sqlStatements = [
             """REPLACE INTO PersonGroup(groupUid, groupMasterCsn, groupLocalCsn, groupLastChangedBy, groupLct, groupName, groupActive, personGroupFlag) 
             VALUES (NEW.groupUid, NEW.groupMasterCsn, NEW.groupLocalCsn, NEW.groupLastChangedBy, NEW.groupLct, NEW.groupName, NEW.groupActive, NEW.personGroupFlag) 
             /*psql ON CONFLICT (groupUid) DO UPDATE 
             SET groupMasterCsn = EXCLUDED.groupMasterCsn, groupLocalCsn = EXCLUDED.groupLocalCsn, groupLastChangedBy = EXCLUDED.groupLastChangedBy, groupLct = EXCLUDED.groupLct, groupName = EXCLUDED.groupName, groupActive = EXCLUDED.groupActive, personGroupFlag = EXCLUDED.personGroupFlag
             */"""
         ]
     )
))
@Entity
@Serializable
@ReplicateEntity(tableId = PersonGroup.TABLE_ID, tracker = PersonGroupReplicate::class)
open class PersonGroup() {

    @PrimaryKey(autoGenerate = true)
    var groupUid: Long = 0

    @MasterChangeSeqNum
    var groupMasterCsn: Long = 0

    @LocalChangeSeqNum
    var groupLocalCsn: Long = 0

    @LastChangedBy
    var groupLastChangedBy: Int = 0

    @LastChangedTime
    @ReplicationVersionId
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
