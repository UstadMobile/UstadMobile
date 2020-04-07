package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
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

    @Query("""SELECT * FROM SaleProductPicture WHERE SaleProductPicture.saleProductPictureUid = :productPictureUid""")
    abstract fun findByUidAsync(productPictureUid: Long): SaleProductPicture?

    @SetAttachmentData
    open fun setAttachment(entity: SaleProductPicture, filePath: String) {
        throw Exception(Exception("Shouldn't call the Dao, call the repo instead "))
    }

    @GetAttachmentData
    open fun getAttachmentPath(entity: SaleProductPicture): String? {
        return ""
    }

    @Query(""" SELECT * FROM SaleProductPicture WHERE 
        saleProductPictureSaleProductUid = :saleProductUid AND saleProductPictureIndex = 
         ( SELECT MIN(saleProductPictureIndex) FROM SaleProductPicture WHERE saleProductPictureSaleProductUid = :saleProductUid ) """)
    abstract suspend fun findBySaleProductUidAsync2(saleProductUid: Long): SaleProductPicture?

    @Query("""SELECT * FROM SaleProductPicture WHERE 
        saleProductPictureSaleProductUid = :productUid ORDER BY saleProductPictureIndex ASC""")
    abstract suspend fun findAllByProductByIndexAsync(productUid: Long) : List<SaleProductPicture>

    @Query("""SELECT * FROM SaleProductPicture WHERE 
        saleProductPictureSaleProductUid = :productUid ORDER BY saleProductPictureIndex ASC""")
    abstract fun findAllByProductByIndex(productUid: Long): DataSource.Factory<Int,SaleProductPicture>

    @Query("""SELECT CASE WHEN MAX(saleProductPictureIndex) IS NOT NULL 
        THEN MAX(saleProductPictureIndex) ELSE -1 END FROM SaleProductPicture 
        WHERE saleProductPictureSaleProductUid = :productUid""")
    abstract suspend fun findMaxIndexForSaleProductPicture(productUid : Long): Int

    @Query("""SELECT COUNT(*) FROM SaleProductPicture 
        WHERE saleProductPictureSaleProductUid = :productUid 
        AND saleProductPictureIndex >=0 """)
    abstract suspend fun findTotalNumberOfPicturesForAProduct(productUid: Long): Int

    //TODO: Check logic if it is okay.

    @Query("""UPDATE SaleProductPicture 
        SET saleProductPictureIndex = saleProductPictureIndex + 1 
        , saleProductPictureLCB = (SELECT nodeClientId FROM SyncNode LIMIT 1)
        WHERE saleProductPictureIndex >= :index 
        AND saleProductPictureSaleProductUid = :productUid""")
    abstract suspend fun moveIndexAheadForSaleProductAndIndex(productUid: Long, index: Int): Int

    @Query("""UPDATE SaleProductPicture SET saleProductPictureIndex = :index 
        , saleProductPictureLCB = (SELECT nodeClientId FROM SyncNode LIMIT 1)
        WHERE SaleProductPictureUid = :productPictureUid """)
    abstract suspend fun updateSaleProductPictureIndex(productPictureUid: Long, index: Int): Int

    suspend fun changeIndexForSaleProductPicture(productPictureUid: Long, productUid: Long, index: Int){
        moveIndexAheadForSaleProductAndIndex(productUid, index)
        updateSaleProductPictureIndex(productPictureUid, index)
    }


}
