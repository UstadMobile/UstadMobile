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
}
