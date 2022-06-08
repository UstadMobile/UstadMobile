package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionAttachment.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@EntityWithAttachment
@ReplicateEntity(tableId = TABLE_ID, tracker = CourseAssignmentSubmissionAttachmentReplicate::class)
@Triggers(arrayOf(
        Trigger(
                name = "courseassignmentsubmissionattachment_remote_insert",
                order = Trigger.Order.INSTEAD_OF,
                on = Trigger.On.RECEIVEVIEW,
                events = [Trigger.Event.INSERT],
                sqlStatements = [
                    """REPLACE INTO CourseAssignmentSubmissionAttachment(casaUid, casaSubmissionUid, casaMimeType,casaFileName, casaUri, casaMd5, casaSize, casaTimestamp) 
         VALUES (NEW.casaUid, NEW.casaSubmissionUid, NEW.casaMimeType, NEW.casaFileName, NEW.casaUri, NEW.casaMd5, NEW.casaSize, NEW.casaTimestamp) 
         /*psql ON CONFLICT (casaUid) DO UPDATE 
         SET casaSubmissionUid = EXCLUDED.casaSubmissionUid, casaMimeType = EXCLUDED.casaMimeType, casaFileName = EXCLUDED.casaFileName, casaUri = EXCLUDED.casaUri, casaMd5 = EXCLUDED.casaMd5, casaSize = EXCLUDED.casaSize, casaTimestamp = EXCLUDED.casaTimestamp
         */"""
                    ])
    )
)
@Serializable
class CourseAssignmentSubmissionAttachment {

    @PrimaryKey(autoGenerate = true)
    var casaUid: Long = 0

    var casaSubmissionUid: Long = 0

    var casaMimeType: String? = null

    var casaFileName: String? = null

    @AttachmentUri
    var casaUri: String? = null

    @AttachmentMd5
    var casaMd5: String? = null

    @AttachmentSize
    var casaSize: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var casaTimestamp: Long = 0

    companion object {

        const val TABLE_ID = 90
    }

}