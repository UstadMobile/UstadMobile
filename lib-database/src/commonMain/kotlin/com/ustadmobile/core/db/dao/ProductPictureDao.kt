package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.GetAttachmentData
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.door.annotation.SetAttachmentData
import com.ustadmobile.lib.db.entities.ProductPicture


@Dao
@Repository
abstract class ProductPictureDao : BaseDao<ProductPicture> {

    @SetAttachmentData
    open fun setAttachment(entity: ProductPicture, filePath: String) {

    }

    @GetAttachmentData
    open fun getAttachmentPath(entity: ProductPicture): String? {
        return ""
    }

    @Query("""SELECT * FROM ProductPicture 
        WHERE productPictureProductUid = :productUid
        AND CAST(productPictureActive AS INTEGER) = 1
        ORDER BY productPictureTimestamp DESC LIMIT 1""")
    abstract suspend fun findByProductUidAsync(productUid: Long): ProductPicture?

    @Query("SELECT * FROM ProductPicture where productPictureProductUid = :productUid ORDER BY " + " productPictureTimestamp DESC LIMIT 1")
    abstract fun findByPersonUidLive(productUid: Long): DoorLiveData<ProductPicture?>


    @Update
    abstract suspend fun updateAsync(productPicture: ProductPicture)

    companion object {


    }



}