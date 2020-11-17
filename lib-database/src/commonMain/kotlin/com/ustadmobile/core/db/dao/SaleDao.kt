package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.*

@UmRepository
@Dao
abstract class SaleDao : BaseDao<Sale> {

    @Update
    abstract suspend fun updateAsync(entity: Sale): Int

    @Query(FIND_WITH_LOCATION_AND_CUSTOMER__BY_UID_QUERY)
    abstract suspend fun findWithCustomerAndLocationByUidAsync(uid: Long): SaleWithCustomerAndLocation?

    @Query(FIND_ALL_SALE_LIST_SALES)
    abstract fun findAllSales(leUid: Long): DataSource.Factory<Int,SaleListDetail>

    @Query(""" SELECT Sale.* FROM Sale WHERE CAST(Sale.saleActive AS INTEGER) = 1 """)
    abstract fun findAllSalesList(): List<Sale>



    companion object {

        const val SORT_NAME_ASC = 1

        const val SORT_NAME_DESC = 2

        const val SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)"

        const val FIND_BY_UID_QUERY = """
            SELECT * FROM Sale WHERE Sale.saleUid = :uid AND CAST(Sale.saleActive AS INTEGER) = 1 
        """
        const val FIND_WITH_LOCATION_AND_CUSTOMER__BY_UID_QUERY = """
            SELECT Sale.*, Person.*, Location.*  FROM Sale 
            LEFT JOIN Person ON Person.personUid = Sale.saleCustomerUid 
            LEFT JOIN Location ON Location.locationUid = Sale.saleLocationUid
            WHERE Sale.saleUid = :uid AND CAST(Sale.saleActive AS INTEGER) = 1 
        """

        const val FIND_ALL_SALE_LIST_SALES = """
            SELECT sl.*,
                (SELECT SaleItem.saleItemQuantity 
                  FROM Sale stg 
                  LEFT JOIN SaleItem ON SaleItem.saleItemSaleUid = stg.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1  
                  WHERE stg.saleUid = sl.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1 
                  ORDER BY stg.saleCreationDate ASC LIMIT 1 
                  )  
                  || 'x ' || 
                  (SELECT Product.productName 
                  FROM SaleItem sitg 
                  LEFT JOIN Product ON Product.productUid = sitg.saleItemProductUid 
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
                          (SELECT CASE WHEN Product.productNameDari IS NOT NULL AND Product.productNameDari != '' THEN Product.productNameDari ELSE Product.productName END 
                          FROM SaleItem sitg 
                          LEFT JOIN Product ON Product.productUid = sitg.saleItemProductUid 
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
                          (SELECT CASE WHEN Product.productNamePashto IS NOT NULL AND Product.productNamePashto != '' THEN Product.productNamePashto ELSE Product.productName END 
                          FROM SaleItem sitg 
                          LEFT JOIN Product ON Product.productUid = sitg.saleItemProductUid 
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
                
                (Select GROUP_CONCAT(Product.productName)  FROM SaleItem 
                  LEFT JOIN Product ON Product.productUid = SaleItem.saleItemProductUid 
                  WHERE SaleItem.saleItemSaleUid = sl.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1 ) AS productNames, 
                  
                (Select GROUP_CONCAT(CASE WHEN Product.productNameDari IS NOT NULL 
                        AND Product.productNameDari != '' THEN Product.productNameDari ELSE Product.productName END)  FROM SaleItem 
                  LEFT JOIN Product ON Product.productUid = SaleItem.saleItemProductUid 
                  WHERE SaleItem.saleItemSaleUid = sl.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1 ) AS productNamesDari,
                   
                '' AS locationName, 
                
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
                    ) 
                , 0) AS earliestDueDate,
                     
                (SELECT count(*) FROM SaleItem WHERE SaleItem.saleItemSaleUid = sl.saleUid) AS saleItemCount,
                    COALESCE((SELECT SUM(SalePayment.salePaymentPaidAmount) FROM SalePayment  
                    WHERE SalePayment.salePaymentSaleUid = sl.saleUid 
                    AND SalePayment.salePaymentDone = 1 AND CAST(SalePayment.salePaymentActive AS INTEGER) = 1 ) ,0) 
                AS saleAmountPaid,
                 
                (select (case  when  
                    (SELECT count(*) from SaleItem sip where sip.saleItemSaleUid = sl.saleUid 
                    AND sip.saleItemPreOrder = 1 ) > 0  then 1  else 0 end)  
                from Sale) AS saleItemPreOrder
                 
                FROM Sale sl 
                
                LEFT JOIN SaleItem ON SaleItem.saleItemSaleUid = sl.saleUid 
                    AND CAST(SaleItem.saleItemActive AS INTEGER) = 1  
                
                LEFT JOIN Person as WE ON SaleItem.saleItemProducerUid = WE.personUid 
                LEFT JOIN Person as SALELE ON sl.salePersonUid = SALELE.personUid
				LEFT JOIN Person as LE ON LE.personUid = :leUid
                
                WHERE CAST(sl.saleActive AS INTEGER) = 1
                
                AND ( CASE WHEN (CAST(LE.admin as INTEGER) = 1) THEN 1 ELSE 0 END OR sl.salePersonUid = LE.personUid)
                
                GROUP BY saleUid 
        """




    }
}
