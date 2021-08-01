package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = ProductPicture.TABLE_ID,

    notifyOnUpdate = ["""
        SELECT DISTINCT UserSession.usClientNodeId AS deviceId, 
               ${ProductPicture.TABLE_ID} AS tableId 
          FROM ChangeLog
               JOIN Person ON Person.personUid = UserSession.usPersonUid
               JOIN ProductPicture 
                    ON ChangeLog.chTableId = ${ProductPicture.TABLE_ID} 
                       AND ChangeLog.chEntityPk = ProductPicture.productPictureUid
                JOIN UserSession
                   ON UserSession.usPersonUid = Person.personUid
               """],
    syncFindAllQuery = """
        SELECT ProductPicture.*
          FROM UserSession
            JOIN Person ON Person.personUid = UserSession.usPersonUid
            JOIN Product ON Product.productPersonAdded = Person.personUid
                OR CAST(Person.admin AS INTEGER) = 1
            JOIN ProductPicture
                ON ProductPicture.productPictureProductUid = Product.productUid
                OR CAST(Person.admin AS INTEGER) = 1
         WHERE UserSession.usClientNodeId = :clientId
        """

    )
@Serializable
@EntityWithAttachment
open class ProductPicture() {

    @PrimaryKey(autoGenerate = true)
    var productPictureUid: Long = 0

    var productPictureProductUid : Long = 0

    @MasterChangeSeqNum
    var productPictureMasterCsn: Long = 0

    @LocalChangeSeqNum
    var productPictureLocalCsn: Long = 0

    @LastChangedBy
    var productPictureLastChangedBy: Int = 0

    @AttachmentUri
    var productPictureUri: String? = null

    @AttachmentMd5
    var productPictureMd5: String? = null

    @AttachmentSize
    var productPictureFileSize: Int = 0

    var productPictureTimestamp: Long = 0

    var productPictureMimeType: String? = null

    var productPictureActive: Boolean = true

    companion object {

        const val TABLE_ID = 214
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ProductPicture

        if (productPictureUid != other.productPictureUid) return false
        if (productPictureProductUid != other.productPictureProductUid) return false
        if (productPictureMasterCsn != other.productPictureMasterCsn) return false
        if (productPictureLocalCsn != other.productPictureLocalCsn) return false
        if (productPictureLastChangedBy != other.productPictureLastChangedBy) return false
        if (productPictureUri != other.productPictureUri) return false
        if (productPictureMd5 != other.productPictureMd5) return false
        if (productPictureFileSize != other.productPictureFileSize) return false
        if (productPictureTimestamp != other.productPictureTimestamp) return false
        if (productPictureMimeType != other.productPictureMimeType) return false
        if (productPictureActive != other.productPictureActive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = productPictureUid.hashCode()
        result = 31 * result + productPictureProductUid.hashCode()
        result = 31 * result + productPictureMasterCsn.hashCode()
        result = 31 * result + productPictureLocalCsn.hashCode()
        result = 31 * result + productPictureLastChangedBy
        result = 31 * result + (productPictureUri?.hashCode() ?: 0)
        result = 31 * result + (productPictureMd5?.hashCode() ?: 0)
        result = 31 * result + productPictureFileSize
        result = 31 * result + productPictureTimestamp.hashCode()
        result = 31 * result + (productPictureMimeType?.hashCode() ?: 0)
        result = 31 * result + productPictureActive.hashCode()
        return result
    }


}
