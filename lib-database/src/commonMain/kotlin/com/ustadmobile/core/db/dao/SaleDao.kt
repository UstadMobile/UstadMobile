package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.*


@UmDao(updatePermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = RoleDao.SELECT_ACCOUNT_IS_ADMIN)
@Dao
@UmRepository
abstract class SaleDao : BaseDao<Sale> {


    @Query("SELECT COUNT(*) FROM SALE WHERE salePreOrder = 1 AND CAST(saleActive AS INTEGER) = 1")
    abstract fun removeMe(): DataSource.Factory<Int, Int>


    @Query("SELECT COUNT(*) FROM SALE WHERE salePreOrder = 1 AND CAST(saleActive AS INTEGER) = 1")
    abstract fun preOrderSaleCountProvider(): DataSource.Factory<Int, Int>

    //    @Query("select count(*) from sale where salePreOrder = 1 AND CAST(saleActive AS INTEGER) = 1")
    @Query(" SELECT COUNT(*) FROM (SELECT (select (case  when  " +
            " (SELECT count(*) from SaleItem sip where sip.saleItemSaleUid = sl.saleUid " +
            " and sip.saleItemPreOrder = 1 ) > 0 then 1  else 0 end) from Sale)  as saleItemPreOrder " +
            " FROM Sale sl WHERE CAST(sl.saleActive AS INTEGER) = 1 AND (saleItemPreOrder = 1 OR CAST(salePreOrder AS INTEGER) = 1)) ")
    abstract fun preOrderSaleCountLive(): Int
    
    //INSERT

    @Insert
    abstract override suspend fun insertAsync(entity: Sale): Long

    @Query(ALL_SALES_ACTIVE_QUERY)
    abstract fun findAllActiveLive(): DoorLiveData<List<Sale>>

    @Query(ALL_SALE_LIST + GROUP_BY)
    abstract fun findAllActiveAsSaleListDetailProvider(leUid: Long): DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST + FILTER_PREORDER + GROUP_BY)
    abstract fun findAllActiveSaleListDetailPreOrdersProvider(leUid: Long): DataSource.Factory<Int,SaleListDetail>

    //Payments due shows the payment amount pending vs the total amount of the sale.
    @Query(ALL_SALE_LIST + FILTER_PAYMENT_DUE + GROUP_BY)
    abstract fun findAllActiveSaleListDetailPaymentDueProvider(leUid: Long): DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST + GROUP_BY + SORT_NAME_ASC )
    abstract fun findAllSaleFilterAllSortNameAscProvider(leUid: Long): DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST + GROUP_BY + SORT_NAME_DEC)
    abstract fun findAllSaleFilterAllSortNameDescProvider(leUid: Long): DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST + GROUP_BY+ SORT_TOTAL_AMOUNT_ASC)
    abstract fun findAllSaleFilterAllSortTotalAscProvider(leUid: Long): DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST + GROUP_BY + SORT_TOTAL_AMOUNT_DESC)
    abstract fun findAllSaleFilterAllSortTotalDescProvider(leUid: Long): DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST + GROUP_BY + SORT_ORDER_DATE_DESC )
    abstract fun findAllSaleFilterAllSortDateAscProvider(leUid: Long): DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST + GROUP_BY + SORT_ORDER_DATE_ASC )
    abstract fun findAllSaleFilterAllSortDateDescProvider(leUid: Long): DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST + FILTER_PREORDER + GROUP_BY + SORT_NAME_ASC )
    abstract fun findAllSaleFilterPreOrderSortNameAscProvider(leUid: Long): DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST + FILTER_PREORDER + GROUP_BY + SORT_NAME_DEC)
    abstract fun findAllSaleFilterPreOrderSortNameDescProvider(leUid: Long): DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST + FILTER_PREORDER + GROUP_BY + SORT_TOTAL_AMOUNT_DESC )
    abstract fun findAllSaleFilterPreOrderSortTotalAscProvider(leUid: Long): DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST + FILTER_PREORDER + GROUP_BY + SORT_TOTAL_AMOUNT_ASC )
    abstract fun findAllSaleFilterPreOrderSortTotalDescProvider(leUid: Long): DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST + FILTER_PREORDER + GROUP_BY + SORT_ORDER_DATE_DESC)
    abstract fun findAllSaleFilterPreOrderSortDateAscProvider(leUid: Long): DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST + FILTER_PREORDER + GROUP_BY + SORT_ORDER_DATE_ASC )
    abstract fun findAllSaleFilterPreOrderSortDateDescProvider(leUid: Long): DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST_WE_FILTER + GROUP_BY + SORT_NAME_ASC )
    abstract fun findAllSaleFilterAllSortNameAscProviderByWeUid(weUid: Long, leUid: Long): DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST_WE_FILTER + GROUP_BY+ SORT_NAME_DEC )
    abstract fun findAllSaleFilterAllSortNameDescProviderByWeUid(weUid: Long, leUid: Long): DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST_WE_FILTER + GROUP_BY + SORT_TOTAL_AMOUNT_ASC)
    abstract fun findAllSaleFilterAllSortTotalAscProviderByWeUid(weUid: Long, leUid: Long): DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST_WE_FILTER + GROUP_BY + SORT_TOTAL_AMOUNT_DESC)
    abstract fun findAllSaleFilterAllSortTotalDescProviderByWeUid(weUid: Long, leUid: Long): DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST_WE_FILTER + GROUP_BY + SORT_ORDER_DATE_ASC )
    abstract fun findAllSaleFilterAllSortDateAscProviderByWeUid(weUid: Long, leUid: Long): DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST_WE_FILTER + GROUP_BY + SORT_ORDER_DATE_DESC)
    abstract fun findAllSaleFilterAllSortDateDescProviderByWeUid(weUid: Long, leUid: Long): DataSource.Factory<Int,SaleListDetail>


    fun filterAndSortSaleByWeUid(leUid: Long, sort:Int, weUid:Long): DataSource.Factory<Int, SaleListDetail>{
        when(sort) {
            SORT_ORDER_NAME_ASC -> return findAllSaleFilterAllSortNameAscProviderByWeUid(weUid, leUid)
            SORT_ORDER_NAME_DESC -> return findAllSaleFilterAllSortNameDescProviderByWeUid(weUid, leUid)
            SORT_ORDER_AMOUNT_ASC -> return findAllSaleFilterAllSortTotalAscProviderByWeUid(weUid, leUid)
            SORT_ORDER_AMOUNT_DESC -> return findAllSaleFilterAllSortTotalDescProviderByWeUid(weUid, leUid)
            SORT_ORDER_DATE_CREATED_DESC -> return findAllSaleFilterAllSortDateAscProviderByWeUid(weUid, leUid)
            SORT_ORDER_DATE_CREATED_ASC -> return findAllSaleFilterAllSortDateDescProviderByWeUid(weUid, leUid)
        }
        return findAllActiveAsSaleListDetailProvider(leUid)
    }

    fun filterAndSortSaleByLeUid(leUid: Long, filter: Int, sort: Int): DataSource.Factory<Int,SaleListDetail> {

        when (filter) {
            ALL_SELECTED -> when (sort) {
                SORT_ORDER_NAME_ASC -> return findAllSaleFilterAllSortNameAscProvider(leUid)
                SORT_ORDER_NAME_DESC -> return findAllSaleFilterAllSortNameDescProvider(leUid)
                SORT_ORDER_AMOUNT_ASC -> return findAllSaleFilterAllSortTotalAscProvider(leUid)
                SORT_ORDER_AMOUNT_DESC -> return findAllSaleFilterAllSortTotalDescProvider(leUid)
                SORT_ORDER_DATE_CREATED_DESC -> return findAllSaleFilterAllSortDateAscProvider(leUid)
                SORT_ORDER_DATE_CREATED_ASC -> return findAllSaleFilterAllSortDateDescProvider(leUid)
            }
            PREORDER_SELECTED -> when (sort) {
                SORT_ORDER_NAME_ASC -> return findAllSaleFilterPreOrderSortNameAscProvider(leUid)
                SORT_ORDER_NAME_DESC -> return findAllSaleFilterPreOrderSortNameDescProvider(leUid)
                SORT_ORDER_AMOUNT_ASC -> return findAllSaleFilterPreOrderSortTotalAscProvider(leUid)
                SORT_ORDER_AMOUNT_DESC -> return findAllSaleFilterPreOrderSortTotalDescProvider(leUid)
                SORT_ORDER_DATE_CREATED_DESC -> return findAllSaleFilterPreOrderSortDateAscProvider(leUid)
                SORT_ORDER_DATE_CREATED_ASC -> return findAllSaleFilterPreOrderSortDateDescProvider(leUid)
            }
            PAYMENT_SELECTED -> {
            }
        }
        return findAllActiveAsSaleListDetailProvider(leUid)
    }

    fun filterAndSortSale(leUid: Long, filter: Int, search: String, sort: Int): DataSource.Factory<Int,SaleListDetail> {

        when (filter) {
            ALL_SELECTED -> when (sort) {
                SORT_ORDER_NAME_ASC -> return findAllSaleFilterAllSortNameAscProvider(leUid)
                SORT_ORDER_NAME_DESC -> return findAllSaleFilterAllSortNameDescProvider(leUid)
                SORT_ORDER_AMOUNT_ASC -> return findAllSaleFilterAllSortTotalAscProvider(leUid)
                SORT_ORDER_AMOUNT_DESC -> return findAllSaleFilterAllSortTotalDescProvider(leUid)
                SORT_ORDER_DATE_CREATED_DESC -> return findAllSaleFilterAllSortDateAscProvider(leUid)
                SORT_ORDER_DATE_CREATED_ASC -> return findAllSaleFilterAllSortDateDescProvider(leUid)
            }
            PREORDER_SELECTED -> when (sort) {
                SORT_ORDER_NAME_ASC -> return findAllSaleFilterPreOrderSortNameAscProvider(leUid)
                SORT_ORDER_NAME_DESC -> return findAllSaleFilterPreOrderSortNameDescProvider(leUid)
                SORT_ORDER_AMOUNT_ASC -> return findAllSaleFilterPreOrderSortTotalAscProvider(leUid)
                SORT_ORDER_AMOUNT_DESC -> return findAllSaleFilterPreOrderSortTotalDescProvider(leUid)
                SORT_ORDER_DATE_CREATED_DESC -> return findAllSaleFilterPreOrderSortDateAscProvider(leUid)
                SORT_ORDER_DATE_CREATED_ASC -> return findAllSaleFilterPreOrderSortDateDescProvider(leUid)
            }
            PAYMENT_SELECTED -> {
            }
        }
        return findAllActiveAsSaleListDetailProvider(leUid)
    }

    @Query(ALL_SALE_LIST + SEARCH_BY_QUERY)
    abstract fun findAllSaleItemsWithSearchFilter(leUid: Long, locationuid: Long,
                                                  amountl: Long, amounth: Long, from: Long,
                                                  to: Long, title: String)
            : DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST + SEARCH_BY_QUERY + FILTER_ORDER_BY_DATE_ASC)
    abstract fun findAllSaleItemsWithSearchFilterOrderDateAsc(leUid: Long, locationuid: Long,
                                                              amountl: Long, amounth: Long,
                                                              from: Long, to: Long,
                                                              title: String)
            : DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST + SEARCH_BY_QUERY + FILTER_ORDER_BY_PRICE_ASC)
    abstract fun findAllSaleItemsWithSearchFilterOrderPriceAsc(leUid: Long, locationuid: Long,
                                                               amountl: Long, amounth: Long,
                                                               from: Long, to: Long,
                                                               title: String)
            : DataSource.Factory<Int,SaleListDetail>

    @Query(ALL_SALE_LIST + SEARCH_BY_QUERY + FILTER_ORDER_BY_PRICE_DESC)
    abstract fun findAllSaleItemsWithSearchFilterOrderPriceDesc(leUid: Long, locationuid: Long,
                                                                amountl: Long, amounth: Long,
                                                                from: Long, to: Long,
                                                                title: String)
            : DataSource.Factory<Int,SaleListDetail>


    fun findAllSaleFilterAndSearchProvider(leUid: Long, locationUid: Long,
                   spl: Long, sph: Long, from: Long, to: Long,
                   searchQuery: String, sort: Int): DataSource.Factory<Int,SaleListDetail> {

        when (sort) {
            SORT_MOST_RECENT -> return findAllSaleItemsWithSearchFilterOrderDateAsc(leUid,
                    locationUid, spl, sph, from, to, searchQuery)
            SORT_LOWEST_PRICE -> return findAllSaleItemsWithSearchFilterOrderPriceAsc(leUid,
                    locationUid, spl, sph, from, to, searchQuery)
            SORT_HIGHEST_PRICE -> return findAllSaleItemsWithSearchFilterOrderPriceDesc(leUid,
                    locationUid, spl, sph, from, to, searchQuery)
            else -> return findAllSaleItemsWithSearchFilter(leUid, locationUid,
                    spl, sph, from, to, searchQuery)
        }

    }

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUid(saleUid: Long): Sale?

    @Query(FIND_BY_UID_QUERY)
    abstract suspend fun findByUidAsync(saleUid: Long):Sale?

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUidLive(saleUid: Long): DoorLiveData<Sale?>

    @Query(INACTIVATE_SALE_QUERY)
    abstract fun inactivateEntity(saleUid: Long)

    @Query(INACTIVATE_SALE_QUERY)
    abstract suspend fun inactivateEntityAsync(saleUid: Long): Int


    //UPDATE:

    @Update
    abstract suspend fun updateAsync(entity: Sale):Int


    //Get overdue sale count
    @Query("select count(*) from sale where Sale.saleDueDate < :today " +
            " and Sale.saleDueDate > 0 AND CAST(Sale.saleActive AS INTEGER) = 1")
    abstract suspend fun getOverDueSaleCountAsync(today: Long): Int


    @Query(" SELECT COUNT(*) FROM " +
            "   ( " +
            "   SELECT " +
            "       (select " +
            "           (case  when  " +
            "               (SELECT count(*) from SaleItem sip where sip.saleItemSaleUid = sl.saleUid " +
            "               and CAST(sip.saleItemPreOrder AS INTEGER) = 1 " +
            "               ) " +
            "           > 0 then 1  else 0 end " +
            "       ) from Sale" +
            "   )  as saleItemPreOrder " +
            "   FROM Sale sl " +
            "   LEFT JOIN Person ON Person.personUid = :leUid " +
            "   WHERE CAST(sl.saleActive AS INTEGER) = 1 " +
            "   AND (saleItemPreOrder = 1 OR CAST(salePreOrder AS INTEGER) = 1)" +
            "   AND ( sl.salePersonUid = Person.personUid OR CAST(Person.admin AS INTEGER) = 1 ) " +
            "   ) ")
    abstract fun getPreOrderSaleCountLive(leUid: Long): DoorLiveData<Int>


    //REPORTING:

    @Query(SALE_PERFORMANCE_REPORT_1)
    abstract suspend fun getSalesPerformanceReportSumGroupedByLocation(leUids: List<Long> ,
                                   producerUids:List<Long>, locationUids:List<Long> ,
                                   productTypeUids:List<Long> , fromDate:Long, toDate:Long,
                                   fromPrice:Int, toPrice:Int): List<ReportSalesPerformance>

    @Query(SALE_PERFORMANCE_REPORT_1)
    abstract fun getSalesPerformanceReportSumGroupedByLocationLive(leUids: List<Long> ,
                       producerUids:List<Long>, locationUids:List<Long> ,
                       productTypeUids:List<Long> , fromDate:Long, toDate:Long,
                       fromPrice:Int, toPrice:Int): DoorLiveData<List<ReportSalesPerformance>>

    @Query("SELECT    " +
            " SUM(SaleItem.saleItemQuantity*SaleItem.saleItemPricePerPiece) as totalSalesValue,  " +
            "    LE.firstNames||' '||LE.lastName as leName,   " +
            "   '' as lastActiveOnApp, " +
            "   '' as leRank, " +
            "   LE.personUid as leUid " +
            " FROM SALE    LEFT JOIN SaleItem ON SaleItem.saleItemSaleUid = SALE.saleUid AND SaleItem.saleItemActive " +
            " LEFT JOIN Person as LE ON Sale.salePersonUid = LE.personUid  WHERE   " +
            " CAST(SALE.saleActive AS INTEGER) = 1 AND CAST(SaleItem.saleItemActive AS INTEGER) = 1 " +
            " GROUP BY leUid " +
            " ORDER BY totalSalesValue DESC")
    abstract suspend fun getTopLEs(): List<ReportTopLEs>

    @Query("SELECT    " +
            " SUM(SaleItem.saleItemQuantity*SaleItem.saleItemPricePerPiece) as totalSalesValue,  " +
            "    LE.firstNames||' '||LE.lastName as leName,   " +
            "   '' as lastActiveOnApp, " +
            "   '' as leRank, " +
            "   LE.personUid as leUid " +
            " FROM SALE    LEFT JOIN SaleItem ON SaleItem.saleItemSaleUid = SALE.saleUid AND " +
            " CAST(SaleItem.saleItemActive AS INTEGER) = 1 " +
            " LEFT JOIN Person as LE ON Sale.salePersonUid = LE.personUid  WHERE   " +
            " CAST(SALE.saleActive AS INTEGER) = 1 AND CAST(SaleItem.saleItemActive AS INTEGER) = 1 " +
            " GROUP BY leUid " +
            "  ORDER BY    totalSalesValue DESC")
    abstract fun getTopLEsLive(): DoorLiveData<List<ReportTopLEs>>

    @Query("SELECT   LE.firstNames||' '||LE.lastName as leName, " +
            " (SaleItem.saleItemQuantity*SaleItem.saleItemPricePerPiece) as saleValue,    " +
            "  Sale.saleCreationDate AS saleDate,  " +
            "  SaleProduct.saleProductName as productNames, " +
            "Location.title as locationName " +
            " FROM SALE    LEFT JOIN SaleItem ON SaleItem.saleItemSaleUid = SALE.saleUid AND SaleItem.saleItemActive  " +
            " LEFT JOIN Location ON Sale.saleLocationUid = Location.locationUid  " +
            " LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = SaleItem.saleItemProductUid " +
            " LEFT JOIN Person as LE ON Sale.salePersonUid = LE.personUid  WHERE   " +
            " CAST(SALE.saleActive AS INTEGER) = 1  AND CAST(SaleItem.saleItemActive AS INTEGER) = 1  " +
            "  ORDER BY    saleDate DESC ")
    abstract suspend fun getSaleLog(): List<ReportSalesLog>

    @Query("SELECT   LE.firstNames||' '||LE.lastName as leName, " +
            " (SaleItem.saleItemQuantity*SaleItem.saleItemPricePerPiece) as saleValue,    " +
            "  Sale.saleCreationDate AS saleDate,  " +
            "  SaleProduct.saleProductName as productNames, " +
            "Location.title as locationName " +
            " FROM SALE    LEFT JOIN SaleItem ON SaleItem.saleItemSaleUid = SALE.saleUid  AND " +
            " CAST(SaleItem.saleItemActive AS INTEGER) = 1 " +
            " LEFT JOIN Location ON Sale.saleLocationUid = Location.locationUid  " +
            " LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = SaleItem.saleItemProductUid " +
            " LEFT JOIN Person as LE ON Sale.salePersonUid = LE.personUid  WHERE   " +
            " CAST(SALE.saleActive AS INTEGER) = 1  AND CAST(SaleItem.saleItemActive AS INTEGER) = 1   " +
            "  ORDER BY    saleDate DESC ")
    abstract fun getSaleLogLive(): DoorLiveData<List<ReportSalesLog>>


    //My Women Entrepreneurs
    @Query(MY_WE_BY_LEUID + MY_WE_SORT_BY_NAME_ASC)
    abstract fun getMyWomenEntrepreneursNameAsc(leUid :Long):DataSource.Factory<Int, PersonWithSaleInfo>

    @Query(MY_WE_BY_LEUID + MY_WE_SORT_BY_NAME_DESC)
    abstract fun getMyWomenEntrepreneursNameDesc(leUid :Long):DataSource.Factory<Int, PersonWithSaleInfo>

    @Query(MY_WE_BY_LEUID + MY_WE_SORT_BY_TOTAL_ASC)
    abstract fun getMyWomenEntrepreneursTotalAsc(leUid :Long):DataSource.Factory<Int, PersonWithSaleInfo>

    @Query(MY_WE_BY_LEUID + MY_WE_SORT_BY_TOTAL_DESC)
    abstract fun getMyWomenEntrepreneursTotalDesc(leUid :Long):DataSource.Factory<Int, PersonWithSaleInfo>

    @Query(MY_WE_BY_LEUID)
    abstract fun getMyWomenEntrepreneurs(leUid :Long):DataSource.Factory<Int, PersonWithSaleInfo>


    fun getMyWomenEntrepreneurs(leUid:Long, sort:Int):DataSource.Factory<Int, PersonWithSaleInfo>{

        return when (sort) {
            SORT_ORDER_NAME_ASC -> getMyWomenEntrepreneursNameAsc(leUid)
            SORT_ORDER_NAME_DESC -> getMyWomenEntrepreneursNameDesc(leUid)
            SORT_ORDER_AMOUNT_ASC -> getMyWomenEntrepreneursTotalAsc(leUid)
            SORT_ORDER_AMOUNT_DESC -> getMyWomenEntrepreneursTotalDesc(leUid)
            else -> getMyWomenEntrepreneurs(leUid)
        }
    }

    @Query("SELECT " +
            "  SUM((SaleItem.saleItemPricePerPiece * SaleItem.saleItemQuantity) - SaleItem.saleItemDiscount) AS totalSale, " +
            "   'Product list goes here' AS topProducts, " +
            "    (SELECT CASE WHEN " +
            "   (SELECT PersonPicture.PersonPictureUid FROM PersonPicture " +
            "    WHERE PersonPicture.personPicturePersonUid = Members.personUid " +
            "       ORDER BY PersonPicture.picTimestamp DESC LIMIT 1 ) " +
            "   is NULL THEN 0 ELSE " +
            "   (SELECT PersonPicture.PersonPictureUid FROM PersonPicture " +
            "    WHERE PersonPicture.personPicturePersonUid = Members.personUid " +
            "    ORDER BY PersonPicture.picTimestamp DESC LIMIT 1) " +
            "   END ) as personPictureUid,"+
            "   Members.* " +
            " FROM PersonGroupMember " +
            "   LEFT JOIN Person AS Members ON Members.personUid = PersonGroupMember.groupMemberPersonUid AND Members.active " +
            "   LEFT JOIN SaleItem ON SaleItem.saleItemProducerUid = Members.personUid AND SaleItem.saleItemActive " +
            "   LEFT JOIN Sale ON Sale.saleUid = SaleItem.saleItemSaleUid AND CAST(Sale.saleActive AS INTEGER) = 1" +
            "   LEFT JOIN PersonPicture ON PersonPicture.personPicturePersonUid = Members.personUid " +
            " WHERE PersonGroupMember.groupMemberGroupUid = :groupUid " +
            "   AND (Members.firstNames like :searchBit OR Members.lastName LIKE :searchBit " +
            " OR Members.firstNames||' '||Members.lastName LIKE :searchBit) " +
            " GROUP BY(Members.personUid)")
    abstract fun getMyWomenEntrepreneursSearch(groupUid :Long, searchBit:String):DataSource.Factory<Int, PersonWithSaleInfo>

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

        const val ALL_SALES_ACTIVE_QUERY = "SELECT * FROM Sale WHERE CAST(saleActive AS INTEGER) = 1 "

        const val ALL_SALE_LIST_SELECT =
            """ 
                SELECT sl.*, 
                (SELECT SaleItem.saleItemQuantity 
                  FROM Sale stg 
                  LEFT JOIN SaleItem ON SaleItem.saleItemSaleUid = stg.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1  
                  WHERE stg.saleUid = sl.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1 
                  ORDER BY stg.saleCreationDate ASC LIMIT 1 
                  )  
                  || 'x ' || 
                  (SELECT SaleProduct.saleProductName 
                  FROM SaleItem sitg 
                  LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = sitg.saleItemProductUid 
                  WHERE sitg.saleItemSaleUid = sl.saleUid AND CAST(sitg.saleItemActive AS INTEGER) = 1  
                  ORDER BY sitg.saleItemCreationDate ASC LIMIT 1) 
                  || 
                  (select 
                      (case  
                      when  
                      (SELECT count(*) from SaleItem sid where sid.saleItemSaleUid = sl.saleUid 
                                and CAST(sid.saleItemActive AS INTEGER) = 1 ) > 1 
                      then '...'  
                      else '' 
                  end) 
                  from sale) 
                AS saleTitleGen, 
                
                (SELECT SaleItem.saleItemQuantity 
                          FROM Sale stg 
                          LEFT JOIN SaleItem ON SaleItem.saleItemSaleUid = stg.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1  
                          WHERE stg.saleUid = sl.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1 
                          ORDER BY stg.saleCreationDate ASC LIMIT 1 
                          )  
                          || 'x ' || 
                          (SELECT CASE WHEN SaleProduct.saleProductNameDari IS NOT NULL AND SaleProduct.saleProductNameDari != '' THEN SaleProduct.saleProductNameDari ELSE SaleProduct.saleProductName END 
                          FROM SaleItem sitg 
                          LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = sitg.saleItemProductUid 
                          WHERE sitg.saleItemSaleUid = sl.saleUid AND CAST(sitg.saleItemActive AS INTEGER) = 1  
                          ORDER BY sitg.saleItemCreationDate ASC LIMIT 1) 
                          || 
                          (select 
                              (case  
                              when  
                              (SELECT count(*) from SaleItem sid where sid.saleItemSaleUid = sl.saleUid 
                                and CAST(sid.saleItemActive AS INTEGER) = 1 ) > 1 
                              then '...'  
                              else '' 
                          end) 
                          from sale) 
                        AS saleTitleGenDari, 
                
                (SELECT SaleItem.saleItemQuantity 
                          FROM Sale stg 
                          LEFT JOIN SaleItem ON SaleItem.saleItemSaleUid = stg.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1  
                          WHERE stg.saleUid = sl.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1 
                          ORDER BY stg.saleCreationDate ASC LIMIT 1 
                          )  
                          || 'x ' || 
                          (SELECT CASE WHEN SaleProduct.saleProductNamePashto IS NOT NULL AND SaleProduct.saleProductNamePashto != '' THEN SaleProduct.saleProductNamePashto ELSE SaleProduct.saleProductName END 
                          FROM SaleItem sitg 
                          LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = sitg.saleItemProductUid 
                          WHERE sitg.saleItemSaleUid = sl.saleUid AND CAST(sitg.saleItemActive AS INTEGER) = 1  
                          ORDER BY sitg.saleItemCreationDate ASC LIMIT 1) 
                          || 
                          (select 
                              (case  
                              when  
                              (SELECT count(*) from SaleItem sid where sid.saleItemSaleUid = sl.saleUid 
                                and CAST(sid.saleItemActive AS INTEGER) = 1 ) > 1 
                              then '...'  
                              else '' 
                          end) 
                          from sale) 
                        AS saleTitleGenPashto, 
                
                (Select GROUP_CONCAT(SaleProduct.saleProductName)  FROM SaleItem 
                  LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = SaleItem.saleItemProductUid 
                  WHERE SaleItem.saleItemSaleUid = sl.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1 ) AS saleProductNames, 
                (Select GROUP_CONCAT(CASE WHEN SaleProduct.saleProductNameDari IS NOT NULL AND SaleProduct.saleProductNameDari != '' THEN SaleProduct.saleProductNameDari ELSE SaleProduct.saleProductName END)  FROM SaleItem 
                  LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = SaleItem.saleItemProductUid 
                  WHERE SaleItem.saleItemSaleUid = sl.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1 ) AS saleProductNamesDari, 
                (Select GROUP_CONCAT(CASE WHEN SaleProduct.saleProductNamePashto IS NOT NULL AND SaleProduct.saleProductNamePashto != '' THEN SaleProduct.saleProductNamePashto ELSE SaleProduct.saleProductName END)  FROM SaleItem 
                  LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = SaleItem.saleItemProductUid 
                  WHERE SaleItem.saleItemSaleUid = sl.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1 ) AS saleProductNamesPashto, 
                Location.title AS locationName, 
                COALESCE( (SELECT SUM(SaleItem.saleItemPricePerPiece * SaleItem.saleItemQuantity) - 
                           SUM(Sale.saleDiscount)  FROM Sale LEFT JOIN SaleItem on SaleItem.saleItemSaleUid = 
                           Sale.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1  WHERE Sale.saleUid = sl.saleUid) ,0 
                ) AS saleAmount, 
                (COALESCE( (SELECT SUM(SaleItem.saleItemPricePerPiece * SaleItem.saleItemQuantity) - 
                           SUM(Sale.saleDiscount)  FROM Sale LEFT JOIN SaleItem on SaleItem.saleItemSaleUid = 
                           Sale.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1  WHERE Sale.saleUid = sl.saleUid) ,0 
                          ) - COALESCE((SELECT SUM(SalePayment.salePaymentPaidAmount) FROM SalePayment 
                              WHERE SalePayment.salePaymentSaleUid = sl.saleUid 
                               AND SalePayment.salePaymentDone = 1 AND CAST(SalePayment.salePaymentActive AS INTEGER) = 1 ) ,
                          0)
                ) AS saleAmountDue, 
                'Afs' AS saleCurrency,  
                coalesce(
                   ( 
                   SELECT SaleItem.saleItemDueDate FROM SaleItem LEFT JOIN Sale on Sale.saleUid = 
                      SaleItem.saleItemSaleUid WHERE SaleItem.saleItemSaleUid = sl.saleUid  
                      AND CAST(Sale.saleActive AS INTEGER) = 1  AND SaleItem.saleItemPreOrder = 1 
                    ORDER BY SaleItem.saleItemDueDate ASC LIMIT 1 
                   ) ,0) AS earliestDueDate, 
                (SELECT count(*) FROM SaleItem WHERE SaleItem.saleItemSaleUid = sl.saleUid) AS saleItemCount,
                COALESCE((SELECT SUM(SalePayment.salePaymentPaidAmount) FROM SalePayment  
                  WHERE SalePayment.salePaymentSaleUid = sl.saleUid 
                  AND SalePayment.salePaymentDone = 1 AND CAST(SalePayment.salePaymentActive AS INTEGER) = 1 ) ,0) 
                AS saleAmountPaid, 
                (select (case  when  
                  (SELECT count(*) from SaleItem sip where sip.saleItemSaleUid = sl.saleUid 
                      AND sip.saleItemPreOrder = 1 ) > 0  then 1  else 0 end)  from Sale)  
                AS saleItemPreOrder 
                FROM Sale sl 
            """

        const val ALL_SALE_LIST_LJ1 =
            """
                LEFT JOIN Location ON Location.locationUid = sl.saleLocationUid  
                LEFT JOIN SaleItem ON SaleItem.saleItemSaleUid = sl.saleUid 
                    AND CAST(SaleItem.saleItemActive AS INTEGER) = 1  
            """

        const val ALL_SALE_LIST_LJ2 =
            """ 
                LEFT JOIN Person as WE ON SaleItem.saleItemProducerUid = WE.personUid 
                LEFT JOIN Person as LE ON sl.salePersonUid = LE.personUid 
            """

        const val ALL_SALE_BY_WE_LJ3 = """
            LEFT JOIN InventoryTransaction ON InventoryTransaction.inventoryTransactionSaleItemUid = SaleItem.saleItemUid 
	        LEFT JOIN InventoryItem ON InventoryItem.inventoryItemUid = InventoryTransaction.inventoryTransactionInventoryItemUid 
        """

        const val ALL_SALE_LIST_WHERE = " WHERE CAST(sl.saleActive AS INTEGER) = 1 "
        const val ALL_SALE_LIST_WHERE_WE =
            """
                    AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1 
                    AND CAST(InventoryTransaction.inventoryTransactionActive AS INTEGER) = 1
                    AND InventoryItem.inventoryItemWeUid = :weUid
                    AND InventoryItem.inventoryItemLeUid = :leUid
                    AND InventoryTransaction.inventoryTransactionFromLeUid = :leUid
            """
        const val GROUP_BY = " GROUP BY saleUid  "

        const val ALL_SALE_LIST_WHERE_LE = " AND ( LE.personUid = :leUid OR CASE WHEN " +
                " (CAST(LE.admin as INTEGER) = 1) THEN 0 ELSE 1 END )  "


        const val ALL_SALE_LIST = ALL_SALE_LIST_SELECT + ALL_SALE_LIST_LJ1 + ALL_SALE_LIST_LJ2 +
                ALL_SALE_LIST_WHERE + ALL_SALE_LIST_WHERE_LE

        const val ALL_SALE_LIST_WE_FILTER = ALL_SALE_LIST_SELECT + ALL_SALE_LIST_LJ1 +
                ALL_SALE_LIST_LJ2 + ALL_SALE_BY_WE_LJ3 + ALL_SALE_LIST_WHERE + ALL_SALE_LIST_WHERE_WE
        //filter and sort

        const val FILTER_PREORDER = " AND (saleItemPreOrder = 1 OR salePreOrder = 1)"
        const val FILTER_PAYMENT_DUE = " AND saleAmountPaid < saleAmount "


        const val SORT_NAME_ASC = " ORDER BY LOWER(saleProductNames) ASC "
        const val SORT_NAME_DEC = " ORDER BY LOWER(saleProductNames) DESC "
        const val SORT_TOTAL_AMOUNT_DESC = " ORDER BY saleAmount DESC "
        const val SORT_TOTAL_AMOUNT_ASC = " ORDER BY saleAmount ASC "
        const val SORT_ORDER_DATE_DESC = " ORDER BY sl.saleCreationDate DESC "
        const val SORT_ORDER_DATE_ASC = " ORDER BY sl.saleCreationDate ASC "

        //Filter queries
        //ALL_SALE_LIST
        const val SEARCH_BY_QUERY = " AND " +
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

        //TODO: Replace with boolean arguments
        const val INACTIVATE_SALE_QUERY = "UPDATE Sale SET saleActive = 0 WHERE saleUid = :saleUid"


        const val SALE_PERFORMANCE_REPORT_SELECT_SALE_AMOUNT_SUM = " SELECT " +
        "   SUM(SaleItem.saleItemQuantity*SaleItem.saleItemPricePerPiece) as saleAmount, ";

        const val SALE_PERFORMANCE_REPORT_SELECT_SALE_AMOUNT_AVERAGE = " SELECT " +
        "   AVERAGE(SaleItem.saleItemQuantity*SaleItem.saleItemPricePerPiece) as saleAmount, ";

        const val SALE_PERFORMANCE_REPORT_SELECT_BIT1 =
        "   Location.title as locationName,  " +
        "   Location.locationUid as locationUid, " +
        "   Sale.saleUid, " +
        "   strftime('%Y-%m-%d', Sale.saleCreationDate/1000, 'unixepoch') AS saleCreationDate, " +
        "   strftime('%W-%Y', Sale.saleCreationDate/1000, 'unixepoch') AS dateGroup, ";
        const val SALE_PERFORMANCE_REPORT_SELECT_DATE_WEEKLY =
        "   strftime('%Y-%m-%d', Sale.saleCreationDate/1000, 'unixepoch', 'weekday 6', '-6 day') AS firstDateOccurence, ";
        //TODO:
        const val SALE_PERFORMANCE_REPORT_SELECT_DATE_MONTHLY =
        "   strftime('%Y-%m-%d', Sale.saleCreationDate/1000, 'unixepoch', 'weekday 6', '-6 day') AS firstDateOccurence, ";
        //TODO:
        const val SALE_PERFORMANCE_REPORT_SELECT_DATE_YEARLY =
        "   strftime('%Y-%m-%d', Sale.saleCreationDate/1000, 'unixepoch', 'weekday 6', '-6 day') AS firstDateOccurence, ";


        const val SALE_PERFORMANCE_REPORT_SELECT_BIT2 =
        "   SaleProduct.saleProductName, " +
        "   SaleItem.saleItemQuantity, " +
        "   WE.firstNames||' '||WE.lastName as producerName, " +
        "   WE.personUid as producerUid, " +
        "   LE.firstNames||' '||LE.lastName as leName, " +
        "   LE.personUid as leUid, " +
        "   ''  AS grantee, " +
        "   (SELECT PP.saleProductName FROM SaleProductParentJoin " +
        "   LEFT JOIN SaleProduct AS PP ON SaleProductParentJoin.saleProductParentJoinParentUid = PP.saleProductUid" +
        "   WHERE SaleProductParentJoin.saleProductParentJoinChildUid = SaleItem.saleItemProductUid) as productTypeName, " +
        "   (SELECT PP.saleProductUid FROM SaleProductParentJoin " +
        "   LEFT JOIN SaleProduct AS PP ON SaleProductParentJoin.saleProductParentJoinParentUid = PP.saleProductUid" +
        "   WHERE SaleProductParentJoin.saleProductParentJoinChildUid = SaleItem.saleItemProductUid) as productTypeUid " +
        " FROM SALE " +
        "   LEFT JOIN SaleItem ON SaleItem.saleItemSaleUid = SALE.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1 " +
        "   LEFT JOIN Location ON Sale.saleLocationUid = Location.locationUid " +
        "   LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = SaleItem.saleItemProductUid " +
        "   LEFT JOIN Person as WE ON SaleItem.saleItemProducerUid = WE.personUid " +
        "   LEFT JOIN Person as LE ON Sale.salePersonUid = LE.personUid " +
        " WHERE " +
        "   CAST(SALE.saleActive AS INTEGER) = 1  " +
        "   AND CAST(SaleItem.saleItemActive AS INTEGER) = 1  " +
        "   OR leUid in (:leUids) " +
        "   OR producerUid in (:producerUids) " +
        "   OR locationUid in (:locationUids) " +
        "   OR productTypeUid in (:productTypeUids) " +
        "   AND Sale.saleCreationDate > :fromDate " +
        "   AND Sale.saleCreationDate < :toDate " ;
        const val SALE_PERFORMANCE_REPORT_GROUP_BY_LOCATION =
        " GROUP BY locationName, firstDateOccurence " ;
        const val SALE_PERFORMANCE_REPORT_GROUP_BY_PRODUCT_TYPE =
        " GROUP BY productType, firstDateOccurence " ;
        const val SALE_PERFORMANCE_REPORT_GROUP_BY_GRANTEE =
        " GROUP BY grantee, firstDateOccurence " ;
        const val SALE_PERFORMANCE_REPORT_HAVING_BIT =
        "   HAVING saleAmount > :fromPrice " +
        "   AND saleAmount < :toPrice ";
        const val SALE_PERFORMANCE_REPORT_ORDER_BY_SALE_CREATION_DESC =
        " ORDER BY " +
        "   firstDateOccurence ASC ";

        const val SALE_PERFORMANCE_REPORT_1 =
            SALE_PERFORMANCE_REPORT_SELECT_SALE_AMOUNT_SUM + SALE_PERFORMANCE_REPORT_SELECT_BIT1 +
            SALE_PERFORMANCE_REPORT_SELECT_DATE_WEEKLY + SALE_PERFORMANCE_REPORT_SELECT_BIT2 +
            SALE_PERFORMANCE_REPORT_GROUP_BY_LOCATION + SALE_PERFORMANCE_REPORT_HAVING_BIT +
            SALE_PERFORMANCE_REPORT_ORDER_BY_SALE_CREATION_DESC;

        const val MY_WE_BY_LEUID =
                """
                    SELECT 
                        SUM((SaleItem.saleItemPricePerPiece)) - SaleItem.saleItemDiscount AS totalSale, 
                        GROUP_CONCAT(DISTINCT SaleProduct.saleProductName) AS topProducts, 
                        (SELECT CASE WHEN 
                            (SELECT PersonPicture.PersonPictureUid FROM PersonPicture 
                            WHERE PersonPicture.personPicturePersonUid = WE.personUid 
                            ORDER BY PersonPicture.picTimestamp DESC LIMIT 1 ) 
                            is NULL THEN 0 ELSE 
                            (SELECT PersonPicture.PersonPictureUid FROM PersonPicture 
                            WHERE PersonPicture.personPicturePersonUid = WE.personUid 
                            ORDER BY PersonPicture.picTimestamp DESC LIMIT 1) 
                            END ) as personPictureUid ,
                        WE.* 
                    FROM PersonGroupMember 
                        LEFT JOIN Person AS LE ON LE.personUid = :leUid
                        LEFT JOIN Person AS WE ON WE.personUid = PersonGroupMember.groupMemberPersonUid 
                        LEFT JOIN InventoryItem ON 
                            InventoryItem.InventoryItemWeUid = WE.personUid 
                            AND InventoryItem.InventoryItemLeUid = LE.personUid 
                        LEFT JOIN InventoryTransaction ON 
                            InventoryTransaction.InventoryTransactionInventoryItemUid = InventoryItem.InventoryItemUid 
                            AND InventoryTransaction.inventoryTransactionFromLeUid = LE.personUid 
                        LEFT JOIN SaleItem ON 
                            SaleItem.saleItemUid = InventoryTransaction.inventoryTransactionSaleItemUid 
                        LEFT JOIN Sale ON 
                            Sale.saleUid = SaleItem.saleItemSaleUid 
                        LEFT JOIN PersonPicture ON PersonPicture.personPicturePersonUid = WE.personUid 
                        LEFT JOIN SaleProduct ON SaleProduct.saleProductUid = SaleItem.saleItemProductUid
                        
                    WHERE PersonGroupMember.groupMemberGroupUid = LE.mPersonGroupUid 
                    AND CAST(Sale.saleActive AS INTEGER) = 1  
                    AND CAST(SaleItem.saleItemActive AS INTEGER) = 1 
                    AND CAST(InventoryTransaction.inventoryTransactionActive AS INTEGER) = 1
                    AND CAST(WE.active AS INTEGER) = 1  
                    AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1
                    
                    GROUP BY(WE.personUid)  
                """

        const val MY_WE_SORT_BY_NAME_ASC = " ORDER BY WE.firstNames ASC, WE.lastName ASC"
        const val MY_WE_SORT_BY_NAME_DESC = " ORDER BY WE.firstNames DESC, WE.lastName DESC"
        const val MY_WE_SORT_BY_TOTAL_ASC = " ORDER BY totalSale ASC"
        const val MY_WE_SORT_BY_TOTAL_DESC = " ORDER BY totalSale DESC"

    }


}
