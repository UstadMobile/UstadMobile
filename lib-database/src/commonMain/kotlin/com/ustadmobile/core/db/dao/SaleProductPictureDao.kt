package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.DoorLiveData
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

//TODO: Fix KMP
//    @UmDbSetAttachment
//    abstract fun setAttachment(uid: Long, pictureAttachment: InputStream)
//
//    @UmDbGetAttachment
//    abstract fun getAttachmentPath(uid: Long): String
//
//    @UmDbGetAttachment
//    abstract fun getAttachmentStream(uid: Long): InputStream
//
//    @UmDbSetAttachment
//    abstract fun setAttachmentFromTmpFile(uid: Long, tmpFilePath: String)

    @Query("SELECT * FROM SaleProductPicture where " +
            " saleProductPictureSaleProductUid = :saleProductUid " +
            " ORDER BY saleProductPictureTimestamp DESC LIMIT 1")
    abstract suspend fun findBySaleProductUidAsync(saleProductUid: Long)
            :SaleProductPicture?

//TODO: Fix KMP
//    @UmDbSetAttachment
//    @UmRestAccessible
//    @UmRepository(delegateType = UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE)
//    abstract fun uploadAttachment(uid: Long, attachment: InputStream)
//
//    @UmDbGetAttachment
//    @UmRestAccessible
//    @UmRepository(delegateType = UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE)
//    abstract fun downloadAttachment(uid: Long): InputStream


}
