package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@Serializable
@EntityWithAttachment
@ReplicateEntity(tableId = CoursePicture.TABLE_ID, tracker = CoursePictureReplicate::class)
@Triggers(arrayOf(
 Trigger(
     name = "coursepicture_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO CoursePicture(coursePictureUid, coursePictureClazzUid, coursePictureMasterCsn, coursePictureLocalCsn, coursePictureLastChangedBy, coursePictureLct, coursePictureUri, coursePictureMd5, coursePictureFileSize, coursePictureTimestamp, coursePictureMimeType, coursePictureActive) 
         VALUES (NEW.coursePictureUid, NEW.coursePictureClazzUid, NEW.coursePictureMasterCsn, NEW.coursePictureLocalCsn, NEW.coursePictureLastChangedBy, NEW.coursePictureLct, NEW.coursePictureUri, NEW.coursePictureMd5, NEW.coursePictureFileSize, NEW.coursePictureTimestamp, NEW.coursePictureMimeType, NEW.coursePictureActive) 
         /*psql ON CONFLICT (coursePictureUid) DO UPDATE 
         SET coursePictureClazzUid = EXCLUDED.coursePictureClazzUid, coursePictureMasterCsn = EXCLUDED.coursePictureMasterCsn, coursePictureLocalCsn = EXCLUDED.coursePictureLocalCsn, coursePictureLastChangedBy = EXCLUDED.coursePictureLastChangedBy, coursePictureLct = EXCLUDED.coursePictureLct, coursePictureUri = EXCLUDED.coursePictureUri, coursePictureMd5 = EXCLUDED.coursePictureMd5, coursePictureFileSize = EXCLUDED.coursePictureFileSize, coursePictureTimestamp = EXCLUDED.coursePictureTimestamp, coursePictureMimeType = EXCLUDED.coursePictureMimeType, coursePictureActive = EXCLUDED.coursePictureActive
         */"""
     ]
 )
))
open class CoursePicture() {

    @PrimaryKey(autoGenerate = true)
    var coursePictureUid: Long = 0

    var coursePictureClazzUid: Long = 0

    @MasterChangeSeqNum
    var coursePictureMasterCsn: Long = 0

    @LocalChangeSeqNum
    var coursePictureLocalCsn: Long = 0

    @LastChangedBy
    var coursePictureLastChangedBy: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var coursePictureLct: Long = 0

    @AttachmentUri
    var coursePictureUri: String? = null

    @AttachmentMd5
    var coursePictureMd5: String? = null

    @AttachmentSize
    var coursePictureFileSize: Int = 0

    var coursePictureTimestamp: Long = 0

    var coursePictureMimeType: String? = null

    var coursePictureActive: Boolean = true

    companion object {

        const val TABLE_ID = 125
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CoursePicture

        if (coursePictureUid != other.coursePictureUid) return false
        if (coursePictureClazzUid != other.coursePictureClazzUid) return false
        if (coursePictureMasterCsn != other.coursePictureMasterCsn) return false
        if (coursePictureLocalCsn != other.coursePictureLocalCsn) return false
        if (coursePictureLastChangedBy != other.coursePictureLastChangedBy) return false
        if (coursePictureLct != other.coursePictureLct) return false
        if (coursePictureUri != other.coursePictureUri) return false
        if (coursePictureMd5 != other.coursePictureMd5) return false
        if (coursePictureFileSize != other.coursePictureFileSize) return false
        if (coursePictureTimestamp != other.coursePictureTimestamp) return false
        if (coursePictureMimeType != other.coursePictureMimeType) return false
        if (coursePictureActive != other.coursePictureActive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = coursePictureUid.hashCode()
        result = 31 * result + coursePictureClazzUid.hashCode()
        result = 31 * result + coursePictureMasterCsn.hashCode()
        result = 31 * result + coursePictureLocalCsn.hashCode()
        result = 31 * result + coursePictureLastChangedBy
        result = 31 * result + coursePictureLct.hashCode()
        result = 31 * result + (coursePictureUri?.hashCode() ?: 0)
        result = 31 * result + (coursePictureMd5?.hashCode() ?: 0)
        result = 31 * result + coursePictureFileSize
        result = 31 * result + coursePictureTimestamp.hashCode()
        result = 31 * result + (coursePictureMimeType?.hashCode() ?: 0)
        result = 31 * result + coursePictureActive.hashCode()
        return result
    }


}
