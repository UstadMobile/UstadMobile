package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity

@SyncableEntity(tableId = 73)
@Entity
class SaleProductPicture() {

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
}
