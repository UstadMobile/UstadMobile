package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

/**
 * This class mediates the relationship between a person and a clazz. A member can be a teacher,
 * or a student. Each member has a joining date, and a leaving date.
 */

@Entity(indices = [
    //Index to optimize SchoolList where it selects a count of the members of each school by role.
    Index(value = ["schoolMemberSchoolUid", "schoolMemberActive", "schoolMemberRole"])
])
@Serializable
@ReplicateEntity(
    tableId = SchoolMember.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(arrayOf(
 Trigger(
     name = "schoolmember_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
     sqlStatements = [ TRIGGER_UPSERT, ]
 )
))
open class SchoolMember {

    @PrimaryKey(autoGenerate = true)
    var schoolMemberUid: Long = 0

    @ColumnInfo(index = true)
    var schoolMemberPersonUid: Long = 0

    @ColumnInfo(index = true)
    var schoolMemberSchoolUid: Long = 0

    var schoolMemberJoinDate : Long = 0

    var schoolMemberLeftDate : Long = Long.MAX_VALUE

    var schoolMemberRole: Int = 0

    var schoolMemberActive: Boolean = true

    @LocalChangeSeqNum
    var schoolMemberLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var schoolMemberMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var schoolMemberLastChangedBy: Int = 0

    @ReplicateLastModified
    @ReplicateEtag
    var schoolMemberLct: Long = 0


    constructor(){
        schoolMemberActive = true
        schoolMemberLeftDate = Long.MAX_VALUE
    }

    companion object {
        const val TABLE_ID = 200


    }
}
