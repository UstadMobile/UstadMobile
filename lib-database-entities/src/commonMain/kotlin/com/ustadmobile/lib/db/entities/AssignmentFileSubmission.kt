package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.AssignmentFileSubmission.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(tableId = TABLE_ID, tracker = AssignmentFileSubmissionReplicate::class)
@Triggers(arrayOf(
        Trigger(
                name = "assignmentfilesubmission_remote_insert",
                order = Trigger.Order.INSTEAD_OF,
                on = Trigger.On.RECEIVEVIEW,
                events = [Trigger.Event.INSERT],
                sqlStatements = [
                    """REPLACE INTO AssignmentFileSubmission(afsUid, afsAssignmentUid, afsStudentUid, afsTimestamp, afsTitle, afsSubmitted, afsActive, afsUri, afsMd5, afsSize, afsMasterCsn, afsLocalCsn, afsLastChangedBy, afsLct) 
         VALUES (NEW.afsUid, NEW.afsAssignmentUid, NEW.afsStudentUid, NEW.afsTimestamp, NEW.afsTitle, NEW.afsSubmitted, NEW.afsActive, NEW.afsUri, NEW.afsMd5, NEW.afsSize, NEW.afsMasterCsn, NEW.afsLocalCsn, NEW.afsLastChangedBy, NEW.afsLct) 
         /*psql ON CONFLICT (caUid) DO UPDATE 
         SET afsUid = EXCLUDED.afsUid, afsAssignmentUid = EXCLUDED.afsAssignmentUid, afsStudentUid = EXCLUDED.afsStudentUid, afsTimestamp = EXCLUDED.afsTimestamp, afsTitle = EXCLUDED.afsTitle, afsSubmitted = EXCLUDED.afsSubmitted, afsActive = EXCLUDED.afsActive, afsUri = EXCLUDED.afsUri, afsMd5 = EXCLUDED.afsMd5, afsSize = EXCLUDED.afsSize, afsMasterCsn = EXCLUDED.afsMasterCsn, afsLocalCsn = EXCLUDED.afsLocalCsn, afsLastChangedBy =  EXCLUDED.afsLastChangedBy, afsLct = EXCLUDED.afsLct
         */"""
                    ])
    )
)
@Serializable
class AssignmentFileSubmission {

    @PrimaryKey(autoGenerate = true)
    var afsUid: Long = 0

    var afsAssignmentUid: Long = 0

    var afsStudentUid: Long = 0

    var afsTimestamp: Long = 0

    var afsMimeType: String? = null

    var afsTitle: String? = null

    var afsSubmitted: Boolean = false

    var afsActive = true

    @AttachmentUri
    var afsUri: String? = null

    @AttachmentMd5
    var afsMd5: String? = null

    @AttachmentSize
    var afsSize: Int = 0

    @MasterChangeSeqNum
    var afsMasterCsn: Long = 0

    @LocalChangeSeqNum
    var afsLocalCsn: Long = 0

    @LastChangedBy
    var afsLastChangedBy: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var afsLct: Long = 0

    companion object {

        const val TABLE_ID = 90
    }

}