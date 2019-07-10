package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 73)
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

    @UmSyncMasterChangeSeqNum
    var saleProductPictureMCSN: Long = 0

    @UmSyncLocalChangeSeqNum
    var saleProductPictureLCSN: Long = 0

    @UmSyncLastChangedBy
    var saleProductPictureLCB: Int = 0
}
