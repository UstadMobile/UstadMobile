package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.*
import com.ustadmobile.lib.db.entities.SaleItem
import com.ustadmobile.lib.db.entities.SaleItemListDetail

@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
abstract class SaleItemDao : SyncableDao<SaleItem, SaleItemDao> {

    //INSERT



    @Query(GENERATE_SALE_NAME)
    abstract fun getTitleForSaleUid(saleUid: Long): String

    @Query(GENERATE_SALE_NAME)
    abstract fun getTitleForSaleUidAsync(saleUid: Long, resultCallback: UmCallback<String>)

    //FIND ALL ACTIVE

    @Query("SELECT * FROM SaleItem")
    abstract fun findAllList(): List<SaleItem>

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveLive(): UmLiveData<List<SaleItem>>

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveList(): List<SaleItem>

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveAsync(allActiveCallback: UmCallback<List<SaleItem>>)

    @Query(ALL_ACTIVE_QUERY)
    abstract fun findAllActiveProvider(): UmProvider<SaleItem>

    @Query("SELECT count(*) From SaleItem where SaleItem.saleItemSaleUid = :saleUid AND SaleItem.saleItemActive = 1")
    abstract fun getSaleItemCountFromSale(saleUid: Long, resultCallback: UmCallback<Int>)

    @Query(ALL_ACTIVE_SALE_ITEM_LIST_DETAIL_QUERY)
    abstract fun findAllSaleItemListDetailActiveLive(): UmLiveData<List<SaleItemListDetail>>

    @Query(ALL_ACTIVE_SALE_ITEM_LIST_DETAIL_QUERY)
    abstract fun findAllSaleItemListDetailActiveProvider(): UmProvider<SaleItemListDetail>

    @Query(ALL_ACTIVE_SALE_ITEM_LIST_DETAIL_BY_SALE_QUERY)
    abstract fun findAllSaleItemListDetailActiveBySaleLive(saleUid: Long): UmLiveData<List<SaleItemListDetail>>

    @Query(ALL_ACTIVE_SALE_ITEM_LIST_DETAIL_BY_SALE_QUERY)
    abstract fun findAllSaleItemListDetailActiveBySaleProvider(saleUid: Long): UmProvider<SaleItemListDetail>

    @Query(TOTAL_PAID_BY_SALE_UID)
    abstract fun findTotalPaidInASale(saleUid: Long): Long

    @Query(TOTAL_PAID_BY_SALE_UID)
    abstract fun findTotalPaidBySaleAsync(saleUid: Long, resultCallback: UmCallback<Long>)

    @Query(TOTAL_DISCOUNT_BY_SALE_UID)
    abstract fun findTotalDiscountInASale(saleUid: Long): Long

    @Query(TOTAL_DISCOUNT_BY_SALE_UID)
    abstract fun findTotalDiscountBySaleAsync(saleUid: Long, resultCallback: UmCallback<Long>)

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUid(uid: Long): SaleItem

    @Query(FIND_BY_UID_QUERY)
    abstract suspend fun findByUidAsync(uid: Long):SaleItem

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUidLive(uid: Long): DoorLiveData<SaleItem>

    @Query(INACTIVATE_QUERY)
    abstract fun inactivateEntity(uid: Long)

    @Query(INACTIVATE_QUERY)
    abstract fun inactivateEntityAsync(uid: Long, inactivateCallback: UmCallback<Int>)


    //UPDATE:

    @Update
    abstract suspend fun updateAsync(entity: SaleItem):Int

    companion object {

        const val GENERATE_SALE_NAME = " SELECT (SELECT SaleItem.saleItemQuantity " +
                "  FROM Sale s " +
                "  LEFT JOIN SaleItem ON SaleItem.saleItemSaleUid = s.saleUid " +
                "  WHERE s.saleUid = :saleUid " +
                "  ORDER BY s.saleCreationDate ASC LIMIT 1) || 'x ' || " +
                "  (SELECT SaleProduct.saleProductName " +
                "  FROM SaleItem i " +
                "  LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = i.saleItemProductUid" +
                "  WHERE i.saleItemSaleUid = :saleUid" +
                "  ORDER BY i.saleItemCreationDate ASC LIMIT 1) " +
                " || " +
                " (select (case  " +
                "  when  " +
                "  (SELECT count(*) from SaleItem si where si.saleItemSaleUid = :saleUid) > 1 " +
                "  then '...'  " +
                "  else '' " +
                "  end) from sale)" +
                "FROM Sale " +
                "where Sale.saleUid = :saleUid "

        const val ALL_ACTIVE_QUERY = "SELECT * FROM SaleItem WHERE saleItemActive = 1"

        /**
         * long saleItemPictureUid;
         * String saleItemProductName;
         * int saleItemQuantityCount;
         * float saleItemPrice;
         * float saleItemDiscountPerItem;
         * boolean saleItemDelivered;
         */
        const val ALL_ACTIVE_SALE_ITEM_LIST_DETAIL_QUERY = "SELECT SaleItem.*, SaleProductPicture.saleProductPictureUid AS saleItemPictureUid, " +
                " SaleProduct.saleProductName AS saleItemProductName " +
                "FROM SaleItem " +
                " LEFT JOIN SaleProduct ON SaleItem.saleItemProductUid = SaleProduct.saleProductUid " +
                " LEFT JOIN SaleProductPicture ON SaleProductPicture.saleProductPictureSaleProductUid = " +
                "   SaleProduct.saleProductUid " +
                "WHERE saleItemActive = 1"

        const val ALL_ACTIVE_SALE_ITEM_LIST_DETAIL_BY_SALE_QUERY = "SELECT SaleItem.*, SaleProductPicture.saleProductPictureUid AS saleItemPictureUid, " +
                " SaleProduct.saleProductName AS saleItemProductName " +
                "FROM SaleItem " +
                " LEFT JOIN SaleProduct ON SaleItem.saleItemProductUid = SaleProduct.saleProductUid " +
                " LEFT JOIN SaleProductPicture ON SaleProductPicture.saleProductPictureSaleProductUid = " +
                "   SaleProduct.saleProductUid " +
                "WHERE saleItemActive = 1 AND SaleItem.saleItemSaleUid = :saleUid"

        //Total amount of every sale per sale uid

        const val TOTAL_PAID_BY_SALE_UID = "SELECT SUM(saleItemPricePerPiece * saleItemQuantity) FROM SaleItem " +
                "WHERE saleItemSaleUid = :saleUid AND saleItemActive = 1 " +
                ""
        const val TOTAL_DISCOUNT_BY_SALE_UID = "SELECT SUM(saleItemDiscount * saleItemQuantity) FROM SaleItem " +
                "WHERE saleItemSaleUid = :saleUid AND saleItemActive = 1 " +
                "AND saleItemSold = 1"


        //LOOK UP

        const val FIND_BY_UID_QUERY = "SELECT * FROM SaleItem WHERE saleItemUid = :uid"

        //INACTIVATE:

        const val INACTIVATE_QUERY = "UPDATE SaleItem SET saleItemActive = 0 WHERE saleItemUid = :uid"
    }

}
