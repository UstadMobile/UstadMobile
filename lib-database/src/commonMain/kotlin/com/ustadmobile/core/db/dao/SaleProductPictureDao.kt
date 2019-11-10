package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.GetAttachmentData
import com.ustadmobile.door.annotation.SetAttachmentData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.SaleProductPicture

@UmDao(hasAttachment = true, permissionJoin = " LEFT JOIN SaleProduct ON SaleProductPicture.saleProductPictureSaleProductUid = SaleProduct.saleProductUid", selectPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
@Dao
abstract class SaleProductPictureDao : BaseDao<SaleProductPicture> {


    @Query("SELECT * FROM SaleProductPicture WHERE SaleProductPicture.saleProductPictureUid = :uid")
    abstract fun findByUidLive(uid: Long): DoorLiveData<SaleProductPicture?>

    @Query("SELECT * FROM SaleProductPicture where saleProductPictureSaleProductUid = :uid ORDER BY " + " saleProductPictureTimestamp DESC LIMIT 1")
    abstract fun findByProductUidLive(uid: Long): DoorLiveData<SaleProductPicture?>


    @SetAttachmentData
    open fun setAttachment(entity: SaleProductPicture, filePath: String) {
        throw Exception(Exception("Shouldn't call the Dao, call the repo instead "))
    }

    @GetAttachmentData
    open fun getAttachmentPath(entity: SaleProductPicture): String? {
        return ""
    }

    @Query(""" SELECT * FROM SaleProductPicture WHERE saleProductPictureSaleProductUid = :saleProductUid AND saleProductPictureIndex = 0""")
    abstract suspend fun findBySaleProductUidAsync2(saleProductUid: Long): SaleProductPicture?


}
