package com.ustadmobile.core.db.dao

import androidx.room.Query
import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.*
import com.ustadmobile.lib.db.entities.SaleProductPicture
import kotlinx.io.IOException
import kotlinx.io.InputStream

@UmDao(hasAttachment = true, permissionJoin = " LEFT JOIN SaleProduct ON SaleProductPicture.saleProductPictureSaleProductUid = SaleProduct.saleProductUid", selectPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
abstract class SaleProductPictureDao : SyncableDao<SaleProductPicture, SaleProductPictureDao> {

    @Query("SELECT * FROM SaleProductPicture WHERE SaleProductPicture.saleProductPictureUid = :uid")
    abstract fun findByUidLive(uid: Long): UmLiveData<SaleProductPicture>

    @Query("SELECT * FROM SaleProductPicture where saleProductPictureSaleProductUid = :uid ORDER BY " + " saleProductPictureTimestamp DESC LIMIT 1")
    abstract fun findByProductUidLive(uid: Long): DoorLiveData<SaleProductPicture>

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

    @Query("SELECT * FROM SaleProductPicture where saleProductPictureSaleProductUid = :saleProductUid ORDER BY saleProductPictureTimestamp DESC LIMIT 1")
    abstract fun findBySaleProductUidAsync(saleProductUid: Long, findByUidCallback: UmCallback<SaleProductPicture>)

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
