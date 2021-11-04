package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
//@SyncableEntity(tableId = PersonPicture.TABLE_ID,
//    notifyOnUpdate = ["""
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId,
//               ${PersonPicture.TABLE_ID} AS tableId
//          FROM ChangeLog
//               JOIN PersonPicture
//                    ON ChangeLog.chTableId = ${PersonPicture.TABLE_ID}
//                       AND ChangeLog.chEntityPk = PersonPicture.personPictureUid
//               JOIN Person
//                    ON Person.personUid = PersonPicture.personPicturePersonUid
//               ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT1}
//                    ${Role.PERMISSION_PERSON_PICTURE_SELECT}
//                    ${Person.JOIN_FROM_PERSON_TO_USERSESSION_VIA_SCOPEDGRANT_PT2}"""],
//    syncFindAllQuery = """
//        SELECT PersonPicture.*
//          FROM UserSession
//                   JOIN PersonGroupMember
//                        ON UserSession.usPersonUid = PersonGroupMember.groupMemberPersonUid
//                   ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT1}
//                        ${Role.PERMISSION_PERSON_PICTURE_SELECT}
//                        ${Person.JOIN_FROM_PERSONGROUPMEMBER_TO_PERSON_VIA_SCOPEDGRANT_PT2}
//                   JOIN PersonPicture
//                        ON PersonPicture.personPicturePersonUid = Person.personUid
//         WHERE UserSession.usClientNodeId = :clientId
//               AND UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
//        """
//)
@Serializable
@EntityWithAttachment
open class PersonPicture() {

    @PrimaryKey(autoGenerate = true)
    var personPictureUid: Long = 0

    var personPicturePersonUid: Long = 0

    @MasterChangeSeqNum
    var personPictureMasterCsn: Long = 0

    @LocalChangeSeqNum
    var personPictureLocalCsn: Long = 0

    @LastChangedBy
    var personPictureLastChangedBy: Int = 0

    @LastChangedTime
    var personPictureLct: Long = 0

    @AttachmentUri
    var personPictureUri: String? = null

    @AttachmentMd5
    var personPictureMd5: String? = null

    @AttachmentSize
    var fileSize: Int = 0

    var picTimestamp: Long = 0

    var mimeType: String? = null

    var personPictureActive: Boolean = true

    companion object {

        const val TABLE_ID = 50
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PersonPicture

        if (personPictureUid != other.personPictureUid) return false
        if (personPicturePersonUid != other.personPicturePersonUid) return false
        if (personPictureMasterCsn != other.personPictureMasterCsn) return false
        if (personPictureLocalCsn != other.personPictureLocalCsn) return false
        if (personPictureLastChangedBy != other.personPictureLastChangedBy) return false
        if (fileSize != other.fileSize) return false
        if (picTimestamp != other.picTimestamp) return false
        if (mimeType != other.mimeType) return false
        if (personPictureActive != other.personPictureActive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = personPictureUid.hashCode()
        result = 31 * result + personPicturePersonUid.hashCode()
        result = 31 * result + personPictureMasterCsn.hashCode()
        result = 31 * result + personPictureLocalCsn.hashCode()
        result = 31 * result + personPictureLastChangedBy
        result = 31 * result + fileSize
        result = 31 * result + picTimestamp.hashCode()
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        result = 31 * result + (if(personPictureActive) 1 else 0)
        return result
    }


}
