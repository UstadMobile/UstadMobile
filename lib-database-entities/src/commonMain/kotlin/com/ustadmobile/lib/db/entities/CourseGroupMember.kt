package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(tableId = CourseGroupMember.TABLE_ID, tracker = CourseGroupMemberReplicate::class)
@Triggers(arrayOf(
    Trigger(
        name = "coursegroupmember_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        sqlStatements = [
            """REPLACE INTO CourseGroupMember(cgmUid, cgmSetUid, cgmGroupNumber, cgmPersonUid, cgmLct) 
         VALUES (NEW.cgmUid, NEW.cgmSetUid, NEW.cgmGroupNumber, NEW.cgmPersonUid, NEW.cgmLct) 
         /*psql ON CONFLICT (cgmUid) DO UPDATE 
         SET cgmSetUid = EXCLUDED.cgmSetUid, cgmGroupNumber = EXCLUDED.cgmGroupNumber, cgmPersonUid = EXCLUDED.cgmPersonUid, cgmLct = EXCLUDED.cgmLct
         */"""
        ]
    )
))
@Serializable
class CourseGroupMember {

    @PrimaryKey(autoGenerate = true)
    var cgmUid: Long = 0

    var cgmSetUid: Long = 0

    // real group numbers start from 1, 0 means this person is not yet in a group
    var cgmGroupNumber: Int = 0

    var cgmPersonUid: Long = 0

    @LastChangedTime
    @ReplicationVersionId
    var cgmLct: Long = 0

    companion object {

        const val TABLE_ID = 243


    }

}