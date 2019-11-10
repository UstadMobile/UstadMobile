package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@SyncableEntity(tableId = 73)
@Entity
@Serializable
@EntityWithAttachment
open class SaleProductPicture() {

    @PrimaryKey(autoGenerate = true)
    var saleProductPictureUid: Long = 0

    //The product uid
    var saleProductPictureSaleProductUid: Long = 0

    //file size of product picture
    var saleProductPictureFileSize: Int = 0

    //picture file's timestamp
    var saleProductPictureTimestamp: Long = 0

    //picture file's mime type
    var saleProductPictureMime: String? = null

    @MasterChangeSeqNum
    var saleProductPictureMCSN: Long = 0

    @LocalChangeSeqNum
    var saleProductPictureLCSN: Long = 0

    @LastChangedBy
    var saleProductPictureLCB: Int = 0

    //Index for which picture. Default to zero
    var saleProductPictureIndex : Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SaleProductPicture

        if (saleProductPictureUid != other.saleProductPictureUid) return false
        if (saleProductPictureSaleProductUid != other.saleProductPictureSaleProductUid) return false
        if (saleProductPictureFileSize != other.saleProductPictureFileSize) return false
        if (saleProductPictureTimestamp != other.saleProductPictureTimestamp) return false
        if (saleProductPictureMime != other.saleProductPictureMime) return false
        if (saleProductPictureMCSN != other.saleProductPictureMCSN) return false
        if (saleProductPictureLCSN != other.saleProductPictureLCSN) return false
        if (saleProductPictureLCB != other.saleProductPictureLCB) return false
        if (saleProductPictureIndex != other.saleProductPictureIndex) return false

        return true
    }

    override fun hashCode(): Int {
        var result = saleProductPictureUid.hashCode()
        result = 31 * result + saleProductPictureSaleProductUid.hashCode()
        result = 31 * result + saleProductPictureFileSize
        result = 31 * result + saleProductPictureTimestamp.hashCode()
        result = 31 * result + (saleProductPictureMime?.hashCode() ?: 0)
        result = 31 * result + saleProductPictureMCSN.hashCode()
        result = 31 * result + saleProductPictureLCSN.hashCode()
        result = 31 * result + saleProductPictureLCB
        result = 31 * result + saleProductPictureIndex
        return result
    }


}
