package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ClazzAssignment.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = TABLE_ID,
        notifyOnUpdate =  [
            """
           SELECT DISTINCT UserSession.usClientNodeId AS deviceId, 
                  $TABLE_ID AS tableId 
            FROM ChangeLog
                 JOIN ClazzAssignment
                     ON ChangeLog.chTableId = $TABLE_ID 
                            AND ClazzAssignment.caUid = ChangeLog.chEntityPk
                 JOIN Clazz 
                    ON Clazz.clazzUid = ClazzAssignment.caClazzUid 
               ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_ASSIGNMENT_SELECT}
                    ${Clazz.JOIN_FROM_CLAZZ_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}                           
        """
        ],
        syncFindAllQuery = """
           SELECT ClazzAssignment.* 
          FROM UserSession
               JOIN PersonGroupMember 
                    ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
               ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT1}
                    ${Role.PERMISSION_ASSIGNMENT_SELECT} 
                    ${Clazz.JOIN_FROM_PERSONGROUPMEMBER_TO_CLAZZ_VIA_SCOPEDGRANT_PT2}
               JOIN ClazzAssignment
                    ON ClazzAssignment.caClazzUid = Clazz.clazzUid
         WHERE UserSession.usClientNodeId = :clientId
               AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
        """)
@Serializable
open class ClazzAssignment {

    @PrimaryKey(autoGenerate = true)
    var caUid: Long = 0

    var caTitle: String? = null

    var caDescription: String? = null

    var caAssignmentType: Int = 0

    var caDeadlineDate: Long = Long.MAX_VALUE

    var caStartDate: Long = 0

    var caLateSubmissionType: Int = 0

    var caLateSubmissionPenalty: Int = 0

    var caGracePeriodDate: Long = 0

    var caActive: Boolean = true

    var caClassCommentEnabled: Boolean = true

    var caPrivateCommentsEnabled: Boolean = false

    var caRequireFileSubmission: Boolean = true

    var caFileSubmissionWeight: Int = 0

    var caFileType: Int = 0

    var caSizeLimit: Int = 50

    var caNumberOfFiles: Int = 0

    var caEditAfterSubmissionType: Int = 0

    var caMarkingType: Int = 0

    var caMaxScore: Int = 0

    @ColumnInfo(index = true)
    var caClazzUid: Long = 0

    @LocalChangeSeqNum
    var caLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var caMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var caLastChangedBy: Int = 0

    @LastChangedTime
    var caLct: Long = 0

    companion object {

        const val TABLE_ID = 520

        const val ASSIGNMENT_LATE_SUBMISSION_REJECT = 1
        const val ASSIGNMENT_LATE_SUBMISSION_PENALTY = 2
        const val ASSIGNMENT_LATE_SUBMISSION_ACCEPT = 3

        const val ASSIGNMENT_TYPE_INDIVIDUAL = 1
        const val ASSIGNMENT_TYPE_GROUP = 2

        const val EDIT_AFTER_SUBMISSION_TYPE_ALLOWED_DEADLINE = 1
        const val EDIT_AFTER_SUBMISSION_TYPE_ALLOWED_GRACE = 2
        const val EDIT_AFTER_SUBMISSION_TYPE_NOT_ALLOWED = 3

        const val MARKING_TYPE_TEACHER = 1
        const val MARKING_TYPE_PEERS = 2

        const val FILE_TYPE_ANY = 0
        const val FILE_TYPE_DOC = 1
        const val FILE_TYPE_IMAGE = 2
        const val FILE_TYPE_VIDEO = 3
        const val FILE_TYPE_AUDIO = 4

    }


}