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

        /**
         * This version of the where clause will find anyone who has permission to see this. This
         * is needed for updates to the entity itself.
         */
        const val FROM_SCHOOLMEMBER_TO_SCOPEDGRANT_JOIN_ON_PERSON_OR_CLAZZ_PERMISSION_CLAUSE = """
            ((ScopedGrant.sgTableId = ${ScopedGrant.ALL_TABLES}
                  AND ScopedGrant.sgEntityUid = ${ScopedGrant.ALL_ENTITIES})
              OR (ScopedGrant.sgTableId = ${Person.TABLE_ID}
                  AND ScopedGrant.sgEntityUid = SchoolMember.schoolMemberPersonUid)
              OR (ScopedGrant.sgTableId = ${School.TABLE_ID}
                 AND ScopedGrant.sgEntityUid = SchoolMember.schoolMemberSchoolUid))
        """

        const val JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PERSON_OR_CLAZZ_PERMISSION_PT1 = """
            JOIN ScopedGrant
                 ON $FROM_SCHOOLMEMBER_TO_SCOPEDGRANT_JOIN_ON_PERSON_OR_CLAZZ_PERMISSION_CLAUSE
                    AND (ScopedGrant.sgPermissions &
        """

        /**
         * This version of the join clause will only find those who have permission granted by school,
         * e.g. the entity table sync status needs invalidated because they may now have permission
         * over additional entities.
         *
         * E.g. now that someone is a SchoolMember, those who have the PERSON_SELECT permission over
         * the school can now see this person profile, which was not previously the case. That means
         * the Person table must be invalidated for anyone with the PERSON_SELECT permission granted
         * on the school applicable for this SchoolMember.
         *
         * We only need to invalidate the sync status where permission was granted by school. School
         * membership changes do not have any affect on permissions that were acquired by class or
         * person scopes, or superadmin grants.
         */
        const val FROM_SCHOOLMEMBER_TO_SCOPEDGRANT_JOIN_ON_SCHOOL_PERMISSION_CLAUSE = """
            (ScopedGrant.sgTableId = ${School.TABLE_ID}
                 AND ScopedGrant.sgEntityUid = SchoolMember.schoolMemberSchoolUid)
        """

        const val JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_SCHOOOL_PERMISSION_PT1 = """
            JOIN ScopedGrant
                 ON $FROM_SCHOOLMEMBER_TO_SCOPEDGRANT_JOIN_ON_SCHOOL_PERMISSION_CLAUSE
                    AND (ScopedGrant.sgPermissions &
        """


        const val JOIN_FROM_SCHOOLMEMBER_TO_USERSESSION_VIA_SCOPEDGRANT_PT2 = """
            ) > 0  
            JOIN PersonGroupMember 
                   ON ScopedGrant.sgGroupUid = PersonGroupMember.groupMemberGroupUid
            JOIN UserSession
                   ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
                      AND UserSession.usStatus =${UserSession.STATUS_ACTIVE}
        """



    }
}
