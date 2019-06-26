package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.UmLiveData
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Sale
import com.ustadmobile.lib.db.entities.SaleListDetail


@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN, insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
abstract class SaleDao : SyncableDao<Sale, SaleDao> {

    @Query("select count(*) from sale where salePreOrder = 1 AND saleActive = 1")
    abstract fun preOrderSaleCountProvider(): UmProvider<Int>

    //    @Query("select count(*) from sale where salePreOrder = 1 AND saleActive = 1")
    @Query(" SELECT COUNT(*) FROM (SELECT (select (case  when  " +
            " (SELECT count(*) from SaleItem sip where sip.saleItemSaleUid = sl.saleUid " +
            " and sip.saleItemPreOrder = 1 ) > 0 then 1  else 0 end) from Sale)  as saleItemPreOrder " +
            " FROM Sale sl WHERE sl.saleActive = 1  AND (saleItemPreOrder = 1 OR salePreOrder = 1)) ")
    abstract fun preOrderSaleCountLive(): UmLiveData<Int>

    //INSERT

    @Insert
    abstract fun insertAsync(entity: Sale, insertCallback: UmCallback<Long>)

    @Query(ALL_SALES_QUERY)
    abstract fun findAllList(): List<Sale>

    @Query(ALL_SALES_ACTIVE_QUERY)
    abstract fun findAllActiveLive(): UmLiveData<List<Sale>>

    @Query(ALL_SALES_ACTIVE_QUERY)
    abstract fun findAllActiveList(): List<Sale>

    @Query(ALL_SALES_ACTIVE_QUERY)
    abstract fun findAllActiveAsync(allActiveSalesCallback: UmCallback<List<Sale>>)

    @Query(ALL_SALES_ACTIVE_QUERY)
    abstract fun findAllActiveProvider(): UmProvider<Sale>

    @Query("$ALL_SALES_ACTIVE_QUERY AND salePreOrder = 1 ")
    abstract fun findAllActivePreorderSalesLive(): UmLiveData<List<Sale>>

    @Query("$ALL_SALES_ACTIVE_QUERY AND salePaymentDone = 0")
    abstract fun findAllActivePaymentDueSalesLive(): UmLiveData<List<Sale>>

    @Query("SELECT * FROM Sale WHERE saleTitle = :saleTitle AND saleActive = 1")
    abstract fun findAllSaleWithTitleAsync(saleTitle: String, resultCallback: UmCallback<List<Sale>>)

    @Query("SELECT * FROM Sale WHERE saleTitle = :saleTitle AND saleActive = 1")
    abstract fun findAllSaleWithTitle(saleTitle: String): List<Sale>


    @Query(ALL_SALE_LIST)
    abstract fun findAllActiveAsSaleListDetailLive(): UmLiveData<List<SaleListDetail>>

    @Query(ALL_SALE_LIST)
    abstract fun findAllActiveAsSaleListDetailList(): List<SaleListDetail>

    @Query(ALL_SALE_LIST)
    abstract fun findAllActiveAsSaleListDetailAsync(allActiveSalesCallback: UmCallback<List<SaleListDetail>>)

    @Query("$ALL_SALE_LIST AND salePreOrder = 1")
    abstract fun findAllActiveSaleListDetailPreOrdersLive(): UmLiveData<List<SaleListDetail>>

    @Query("$ALL_SALE_LIST AND salePaymentDone = 1")
    abstract fun findAllActiveSaleListDetailPaymentDueLive(): UmLiveData<List<SaleListDetail>>

    @Query(ALL_SALE_LIST)
    abstract fun findAllActiveAsSaleListDetailProvider(): UmProvider<SaleListDetail>

    @Query(ALL_SALE_LIST + FILTER_PREORDER)
    abstract fun findAllActiveSaleListDetailPreOrdersProvider(): UmProvider<SaleListDetail>


    //Payments due shows the payment amount pending vs the total amount of the sale.
    @Query(ALL_SALE_LIST + FILTER_PAYMENT_DUE)
    abstract fun findAllActiveSaleListDetailPaymentDueProvider(): UmProvider<SaleListDetail>

    @Query(ALL_SALE_LIST + SORT_NAME_ASC)
    abstract fun findAllSaleFilterAllSortNameAscProvider(): UmProvider<SaleListDetail>

    @Query(ALL_SALE_LIST + SORT_NAME_DEC)
    abstract fun findAllSaleFilterAllSortNameDescProvider(): UmProvider<SaleListDetail>

    @Query(ALL_SALE_LIST + SORT_TOTAL_AMOUNT_DESC)
    abstract fun findAllSaleFilterAllSortTotalAscProvider(): UmProvider<SaleListDetail>

    @Query(ALL_SALE_LIST + SORT_TOTAL_AMOUNT_ASC)
    abstract fun findAllSaleFilterAllSortTotalDescProvider(): UmProvider<SaleListDetail>

    @Query(ALL_SALE_LIST + SORT_ORDER_DATE_DESC)
    abstract fun findAllSaleFilterAllSortDateAscProvider(): UmProvider<SaleListDetail>

    @Query(ALL_SALE_LIST + SORT_ORDER_DATE_ASC)
    abstract fun findAllSaleFilterAllSortDateDescProvider(): UmProvider<SaleListDetail>

    @Query(ALL_SALE_LIST + FILTER_PREORDER + SORT_NAME_ASC)
    abstract fun findAllSaleFilterPreOrderSortNameAscProvider(): UmProvider<SaleListDetail>

    @Query(ALL_SALE_LIST + FILTER_PREORDER + SORT_NAME_DEC)
    abstract fun findAllSaleFilterPreOrderSortNameDescProvider(): UmProvider<SaleListDetail>

    @Query(ALL_SALE_LIST + FILTER_PREORDER + SORT_TOTAL_AMOUNT_DESC)
    abstract fun findAllSaleFilterPreOrderSortTotalAscProvider(): UmProvider<SaleListDetail>

    @Query(ALL_SALE_LIST + FILTER_PREORDER + SORT_TOTAL_AMOUNT_ASC)
    abstract fun findAllSaleFilterPreOrderSortTotalDescProvider(): UmProvider<SaleListDetail>

    @Query(ALL_SALE_LIST + FILTER_PREORDER + SORT_ORDER_DATE_DESC)
    abstract fun findAllSaleFilterPreOrderSortDateAscProvider(): UmProvider<SaleListDetail>

    @Query(ALL_SALE_LIST + FILTER_PREORDER + SORT_ORDER_DATE_ASC)
    abstract fun findAllSaleFilterPreOrderSortDateDescProvider(): UmProvider<SaleListDetail>


    fun filterAndSortSale(filter: Int, sort: Int): UmProvider<SaleListDetail> {

        when (filter) {
            ALL_SELECTED -> when (sort) {
                SORT_ORDER_NAME_ASC -> return findAllSaleFilterAllSortNameAscProvider()
                SORT_ORDER_NAME_DESC -> return findAllSaleFilterAllSortNameDescProvider()
                SORT_ORDER_AMOUNT_ASC -> return findAllSaleFilterAllSortTotalAscProvider()
                SORT_ORDER_AMOUNT_DESC -> return findAllSaleFilterAllSortTotalDescProvider()
                SORT_ORDER_DATE_CREATED_DESC -> return findAllSaleFilterAllSortDateAscProvider()
                SORT_ORDER_DATE_CREATED_ASC -> return findAllSaleFilterAllSortDateDescProvider()
            }
            PREORDER_SELECTED -> when (sort) {
                SORT_ORDER_NAME_ASC -> return findAllSaleFilterPreOrderSortNameAscProvider()
                SORT_ORDER_NAME_DESC -> return findAllSaleFilterPreOrderSortNameDescProvider()
                SORT_ORDER_AMOUNT_ASC -> return findAllSaleFilterPreOrderSortTotalAscProvider()
                SORT_ORDER_AMOUNT_DESC -> return findAllSaleFilterPreOrderSortTotalDescProvider()
                SORT_ORDER_DATE_CREATED_DESC -> return findAllSaleFilterPreOrderSortDateAscProvider()
                SORT_ORDER_DATE_CREATED_ASC -> return findAllSaleFilterPreOrderSortDateDescProvider()
            }
            PAYMENT_SELECTED -> {
            }
        }
        return findAllActiveAsSaleListDetailProvider()
    }

    fun filterAndSortSale(filter: Int, search: String, sort: Int): UmProvider<SaleListDetail> {

        when (filter) {
            ALL_SELECTED -> when (sort) {
                SORT_ORDER_NAME_ASC -> return findAllSaleFilterAllSortNameAscProvider()
                SORT_ORDER_NAME_DESC -> return findAllSaleFilterAllSortNameDescProvider()
                SORT_ORDER_AMOUNT_ASC -> return findAllSaleFilterAllSortTotalAscProvider()
                SORT_ORDER_AMOUNT_DESC -> return findAllSaleFilterAllSortTotalDescProvider()
                SORT_ORDER_DATE_CREATED_DESC -> return findAllSaleFilterAllSortDateAscProvider()
                SORT_ORDER_DATE_CREATED_ASC -> return findAllSaleFilterAllSortDateDescProvider()
            }
            PREORDER_SELECTED -> when (sort) {
                SORT_ORDER_NAME_ASC -> return findAllSaleFilterPreOrderSortNameAscProvider()
                SORT_ORDER_NAME_DESC -> return findAllSaleFilterPreOrderSortNameDescProvider()
                SORT_ORDER_AMOUNT_ASC -> return findAllSaleFilterPreOrderSortTotalAscProvider()
                SORT_ORDER_AMOUNT_DESC -> return findAllSaleFilterPreOrderSortTotalDescProvider()
                SORT_ORDER_DATE_CREATED_DESC -> return findAllSaleFilterPreOrderSortDateAscProvider()
                SORT_ORDER_DATE_CREATED_ASC -> return findAllSaleFilterPreOrderSortDateDescProvider()
            }
            PAYMENT_SELECTED -> {
            }
        }
        return findAllActiveAsSaleListDetailProvider()
    }

    @Query(ALL_SALE_LIST + SEARCH_BY_QUERY)
    abstract fun findAllSaleItemsWithSearchFilter(locationuid: Long,
                                                  amountl: Long, amounth: Long, from: Long, to: Long, title: String): UmProvider<SaleListDetail>

    @Query(ALL_SALE_LIST + SEARCH_BY_QUERY + FILTER_ORDER_BY_DATE_ASC)
    abstract fun findAllSaleItemsWithSearchFilterOrderDateAsc(locationuid: Long,
                                                              amountl: Long, amounth: Long, from: Long, to: Long, title: String): UmProvider<SaleListDetail>

    @Query(ALL_SALE_LIST + SEARCH_BY_QUERY + FILTER_ORDER_BY_PRICE_ASC)
    abstract fun findAllSaleItemsWithSearchFilterOrderPriceAsc(locationuid: Long,
                                                               amountl: Long, amounth: Long, from: Long, to: Long, title: String): UmProvider<SaleListDetail>

    @Query(ALL_SALE_LIST + SEARCH_BY_QUERY + FILTER_ORDER_BY_PRICE_DESC)
    abstract fun findAllSaleItemsWithSearchFilterOrderPriceDesc(locationuid: Long,
                                                                amountl: Long, amounth: Long, from: Long, to: Long, title: String): UmProvider<SaleListDetail>


    fun findAllSaleFilterAndSearchProvider(locationUid: Long,
                                           spl: Long, sph: Long, from: Long, to: Long, searchQuery: String, sort: Int): UmProvider<SaleListDetail> {

        when (sort) {
            SORT_MOST_RECENT -> return findAllSaleItemsWithSearchFilterOrderDateAsc(
                    locationUid, spl, sph, from, to, searchQuery)
            SORT_LOWEST_PRICE -> return findAllSaleItemsWithSearchFilterOrderPriceAsc(
                    locationUid, spl, sph, from, to, searchQuery)
            SORT_HIGHEST_PRICE -> return findAllSaleItemsWithSearchFilterOrderPriceDesc(
                    locationUid, spl, sph, from, to, searchQuery)
            else -> return findAllSaleItemsWithSearchFilter(locationUid,
                    spl, sph, from, to, searchQuery)
        }

    }

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUid(saleUid: Long): Sale

    @Query(FIND_BY_UID_QUERY)
    abstract suspend fun findByUidAsync(saleUid: Long):Sale

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUidLive(saleUid: Long): DoorLiveData<Sale>

    @Query(INACTIVATE_SALE_QUERY)
    abstract fun inactivateEntity(saleUid: Long)

    @Query(INACTIVATE_SALE_QUERY)
    abstract fun inactivateEntityAsync(saleUid: Long, inactivateSaleCallback: UmCallback<Int>)


    //UPDATE:

    @Update
    abstract suspend fun updateAsync(entity: Sale):Int


    //Get overdue sale count
    @Query("select count(*) from sale where Sale.saleDueDate < :today and Sale.saleDueDate > 0 AND Sale.saleActive = 1")
    abstract fun getOverDueSaleCountAsync(today: Long, resultCallback: UmCallback<Int>)


    @Query(" SELECT COUNT(*) FROM (SELECT (select (case  when  " +
            " (SELECT count(*) from SaleItem sip where sip.saleItemSaleUid = sl.saleUid " +
            " and sip.saleItemPreOrder = 1 ) > 0 then 1  else 0 end) from Sale)  as saleItemPreOrder " +
            " FROM Sale sl WHERE sl.saleActive = 1  AND (saleItemPreOrder = 1 OR salePreOrder = 1)) ")
    abstract fun getPreOrderSaleCountLive(): DoorLiveData<Int?>

    companion object {

        const val ALL_SELECTED = 1
        const val PREORDER_SELECTED = 2
        const val PAYMENT_SELECTED = 3

        const val SORT_ORDER_NAME_ASC = 1
        const val SORT_ORDER_NAME_DESC = 2
        const val SORT_ORDER_AMOUNT_ASC = 3
        const val SORT_ORDER_AMOUNT_DESC = 4
        const val SORT_ORDER_DATE_CREATED_DESC = 5
        const val SORT_ORDER_DATE_CREATED_ASC = 6


        const val SORT_MOST_RECENT = 1
        const val SORT_LOWEST_PRICE = 2
        const val SORT_HIGHEST_PRICE = 3


        //FIND ALL ACTIVE

        const val ALL_SALES_QUERY = "SELECT * FROM Sale"

        const val ALL_SALES_ACTIVE_QUERY = "SELECT * FROM Sale WHERE saleActive = 1"

        const val ALL_SALE_LIST = " SELECT sl.*, " +
                " (SELECT SaleItem.saleItemQuantity " +
                " FROM Sale stg " +
                " LEFT JOIN SaleItem ON SaleItem.saleItemSaleUid = stg.saleUid " +
                " WHERE stg.saleUid = sl.saleUid AND SaleItem.saleItemActive = 1 " +
                " ORDER BY stg.saleCreationDate ASC LIMIT 1 " +
                " )  " +
                " || 'x ' || " +
                " (SELECT SaleProduct.saleProductName " +
                " FROM SaleItem sitg " +
                " LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = sitg.saleItemProductUid " +
                " WHERE sitg.saleItemSaleUid = sl.saleUid AND sitg.saleItemActive = 1 " +
                " ORDER BY sitg.saleItemCreationDate ASC LIMIT 1) " +
                " || " +
                " (select " +
                "  (case  " +
                "   when  " +
                "   (SELECT count(*) from SaleItem sid where sid.saleItemSaleUid = sl.saleUid) > 1 " +
                "   then '...'  " +
                "   else '' " +
                "  end) " +
                " from sale) " +
                " AS saleTitleGen, " +
                " (Select GROUP_CONCAT(SaleProduct.saleProductName)  FROM SaleItem " +
                "   LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = SaleItem.saleItemProductUid " +
                "   WHERE SaleItem.saleItemSaleUid = sl.saleUid) AS saleProductNames," +
                " Location.title AS locationName, " +
                " COALESCE( (SELECT SUM(SaleItem.saleItemPricePerPiece * SaleItem.saleItemQuantity) - " +
                "            SUM(Sale.saleDiscount)  FROM Sale LEFT JOIN SaleItem on SaleItem.saleItemSaleUid = " +
                "            Sale.saleUid WHERE Sale.saleUid = sl.saleUid) ,0 " +
                " ) AS saleAmount, " +
                " (COALESCE( (SELECT SUM(SaleItem.saleItemPricePerPiece * SaleItem.saleItemQuantity) - " +
                "            SUM(Sale.saleDiscount)  FROM Sale LEFT JOIN SaleItem on SaleItem.saleItemSaleUid = " +
                "            Sale.saleUid WHERE Sale.saleUid = sl.saleUid) ,0 " +
                " ) - COALESCE((SELECT SUM(SalePayment.salePaymentPaidAmount) FROM SalePayment " +
                " WHERE SalePayment.salePaymentSaleUid = sl.saleUid " +
                " AND SalePayment.salePaymentDone = 1 AND SalePayment.salePaymentActive = 1) ,0)) AS saleAmountDue, " +
                " 'Afs' AS saleCurrency,  " +
                " coalesce(" +
                "    ( " +
                "    SELECT SaleItem.saleItemDueDate FROM SaleItem LEFT JOIN Sale on Sale.saleUid = " +
                "       SaleItem.saleItemSaleUid WHERE SaleItem.saleItemSaleUid = sl.saleUid  " +
                "       AND Sale.saleActive = 1 AND SaleItem.saleItemPreOrder = 1 " +
                "     ORDER BY SaleItem.saleItemDueDate ASC LIMIT 1 " +
                "    ) ,0) AS earliestDueDate, " +
                " (SELECT count(*) FROM SaleItem WHERE SaleItem.saleItemSaleUid = sl.saleUid) AS saleItemCount," +
                " COALESCE((SELECT SUM(SalePayment.salePaymentPaidAmount) FROM SalePayment  " +
                "  WHERE SalePayment.salePaymentSaleUid = sl.saleUid " +
                "  AND SalePayment.salePaymentDone = 1 AND SalePayment.salePaymentActive = 1) ,0) " +
                "  AS saleAmountPaid, " +
                "  (select (case  when  " +
                "   (SELECT count(*) from SaleItem sip where sip.saleItemSaleUid = sl.saleUid and sip.saleItemPreOrder = 1 ) > 0 " +
                "   then 1  else 0 end) " +
                " from Sale)  as saleItemPreOrder " +
                " FROM Sale sl " +
                " LEFT JOIN Location ON Location.locationUid = sl.saleLocationUid WHERE sl.saleActive = 1  "

        //filter and sort

        const val FILTER_PREORDER = " AND (saleItemPreOrder = 1 OR salePreOrder = 1)"
        const val FILTER_PAYMENT_DUE = " AND saleAmountPaid < saleAmount "


        const val SORT_NAME_ASC = " ORDER BY sl.saleTitle ASC "
        const val SORT_NAME_DEC = " ORDER BY sl.saleTitle DESC "
        const val SORT_TOTAL_AMOUNT_DESC = " ORDER BY saleAmount DESC "
        const val SORT_TOTAL_AMOUNT_ASC = " ORDER BY saleAmount ASC "
        const val SORT_ORDER_DATE_DESC = " ORDER BY sl.saleCreationDate DESC "
        const val SORT_ORDER_DATE_ASC = " ORDER BY sl.saleCreationDate ASC "

        //Filter queries
        //ALL_SALE_LIST
        const private val SEARCH_BY_QUERY = " AND " +
                " sl.saleLocationUid = :locationuid " +
                " AND saleAmount > :amountl AND saleAmount < :amounth " +
                " AND saleProductNames LIKE :title " +
                " OR (sl.saleCreationDate > :from AND sl.saleCreationDate < :to )"
        const private val FILTER_ORDER_BY_DATE_ASC = " ORDER BY sl.saleCreationDate ASC "
        const private val FILTER_ORDER_BY_PRICE_ASC = " ORDER BY saleAmount ASC "
        const private val FILTER_ORDER_BY_PRICE_DESC = " ORDER BY saleAmount DESC "

        //LOOK UP

        const val FIND_BY_UID_QUERY = "SELECT * FROM Sale WHERE saleUid = :saleUid"

        //INACTIVATE:

        const val INACTIVATE_SALE_QUERY = "UPDATE Sale SET saleActive = 0 WHERE saleUid = :saleUid"
    }
}
