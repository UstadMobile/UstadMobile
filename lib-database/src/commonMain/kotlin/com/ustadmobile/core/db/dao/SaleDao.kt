package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.door.annotation.Repository

@Repository
@Dao
abstract class SaleDao : BaseDao<Sale> {

    @Update
    abstract suspend fun updateAsync(entity: Sale): Int

    //Used in SaleEditPresenter to find customer and location to update on sale edit
    @Query(FIND_SALE_WITH_CUSTOMER_AND_LOCATION)
    abstract suspend fun findWithCustomerAndLocationByUidAsync(uid: Long): SaleWithCustomerAndLocation?

    //Query to find all sales in SaleList
    @Query(FIND_ALL_SALES)
    abstract fun findAllSales(
            leUid: Long, filter: Int, searchText: String): DataSource.Factory<Int,SaleListDetail>

    //People tab at bottom navigation - Show all people with Sale info (if any, eg: total sales)
    @Query(FIND_ALL_PEOPLE_WITH_SALEINFO)
    abstract fun findAllPersonWithSaleInfo(leUid: Long, filter: Int,
                                           searchText: String) : DataSource.Factory<Int, PersonWithSaleInfo>

    companion object {

        const val FILTER_ALL = 0
        const val FILTER_LE_ONLY = 1
        const val FILTER_WE_ONLY = 2
        const val FILTER_CUSTOMER_ONLY = 3

        const val FILTER_ALL_SALES = 0
        const val FILTER_PAYMENTS_DUE_SALES = 1


        const val SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)"

        const val FIND_SALE_WITH_CUSTOMER_AND_LOCATION = """
            SELECT Sale.*, Person.*, Location.*  FROM Sale 
            LEFT JOIN Person ON Person.personUid = Sale.saleCustomerUid 
            LEFT JOIN Location ON Location.locationUid = Sale.saleLocationUid
            WHERE Sale.saleUid = :uid AND CAST(Sale.saleActive AS INTEGER) = 1 
        """

        const val FIND_ALL_PEOPLE_WITH_SALEINFO = """
            SELECT 
                coalesce(SUM((SaleItem.saleItemPricePerPiece) * SaleItem.saleItemQuantity) - 
                    (SUM(SaleItem.saleItemDiscount) + SUM(Sale.saleDiscount)), 0) AS totalSale,
                '' AS topProducts, 
                0 as personPictureUid ,
                Person.* 
            FROM Person 
                LEFT JOIN Person AS LE ON LE.personUid = :leUid
                LEFT JOIN InventoryItem ON 
                    InventoryItem.InventoryItemWeUid = Person.personUid 
                    AND InventoryItem.InventoryItemLeUid = LE.personUid AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1
                LEFT JOIN InventoryTransaction ON 
                    InventoryTransaction.InventoryTransactionInventoryItemUid = InventoryItem.InventoryItemUid 
                    AND InventoryTransaction.inventoryTransactionFromLeUid = LE.personUid AND CAST(InventoryTransaction.inventoryTransactionActive AS INTEGER) = 1
                LEFT JOIN Sale ON Sale.salePersonUid = Person.personUid
                    AND CAST(Sale.saleActive AS INTEGER) = 1
                 LEFT JOIN SaleItem ON    SaleItem.saleItemSaleUid = Sale.saleUid 
                    AND CAST(SaleItem.saleItemActive AS INTEGER) = 1               
                LEFT JOIN Product ON Product.productUid = SaleItem.saleItemProductUid 
                    AND CAST(Product.productActive AS INTEGER) = 1
            WHERE CAST(Person.active AS INTEGER) = 1  
            AND (CAST(Person.admin AS INTEGER) = 0 OR CAST(LE.admin AS INTEGER) = 1 )
            AND (Person.personCreatedBy = LE.personUid OR CAST(LE.admin AS INTEGER) = 1)
            AND CASE :filter WHEN $FILTER_ALL THEN Person.personGoldoziType > -1 
                ELSE CAST(Person.admin AS INTEGER) = 0 END
            AND CASE :filter WHEN $FILTER_LE_ONLY THEN Person.personGoldoziType = 1
                ELSE Person.personGoldoziType > -1 END
            AND CASE :filter WHEN $FILTER_WE_ONLY THEN Person.personGoldoziType = 2
                ELSE Person.personGoldoziType > -1 END 
            AND CASE :filter WHEN $FILTER_CUSTOMER_ONLY THEN Person.personGoldoziType = 0  
                ELSE Person.personGoldoziType > -1 END
            AND Person.firstNames ||' '|| Person.lastName LIKE :searchText
            GROUP BY Person.personUid
        """

        const val FIND_ALL_SALES = """
            SELECT sl.*, Customer.*, SalePerson.firstNames||' '||SalePerson.lastName as saleCreator,
                (SELECT SaleItem.saleItemQuantity 
                  FROM Sale stg 
                  LEFT JOIN SaleItem ON SaleItem.saleItemSaleUid = stg.saleUid 
                        AND CAST(SaleItem.saleItemActive AS INTEGER) = 1  
                  WHERE stg.saleUid = sl.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1 
                  ORDER BY stg.saleCreationDate ASC LIMIT 1 
                  )  
                  || 'x ' || 
                  (SELECT case when Product.productName != '' 
                        then Product.productName 
                        else case when Product.productNameDari != '' 
                            then Product.productNameDari 
                            else case when Product.productNamePashto != '' 
                                then Product.productNamePashto 
                                else '' 
                                end  
                            end 
                        end 
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
                  LEFT JOIN SaleItem ON SaleItem.saleItemSaleUid = stg.saleUid 
                    AND CAST(SaleItem.saleItemActive AS INTEGER) = 1  
                  WHERE stg.saleUid = sl.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1 
                  ORDER BY stg.saleCreationDate ASC LIMIT 1 
                  )  
                  || 'x ' || 
                  (SELECT CASE WHEN Product.productNameDari IS NOT NULL AND Product.productNameDari != '' 
                    THEN Product.productNameDari ELSE Product.productName END 
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
                  LEFT JOIN SaleItem ON SaleItem.saleItemSaleUid = stg.saleUid 
                    AND CAST(SaleItem.saleItemActive AS INTEGER) = 1  
                  WHERE stg.saleUid = sl.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1 
                  ORDER BY stg.saleCreationDate ASC LIMIT 1 
                  )  
                  || 'x ' || 
                  (SELECT 
                    CASE WHEN Product.productNamePashto IS NOT NULL AND Product.productNamePashto != '' 
                    THEN Product.productNamePashto ELSE Product.productName END 
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
                
                '' AS productNames, 
                  
                '' AS productNamesDari,
                   
                '' AS locationName, 
                
                COALESCE( (SELECT SUM(SaleItem.saleItemPricePerPiece * SaleItem.saleItemQuantity) - 
                                SUM(Sale.saleDiscount)  
                            FROM Sale LEFT JOIN SaleItem on SaleItem.saleItemSaleUid = 
                                Sale.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1  
                            WHERE Sale.saleUid = sl.saleUid) ,
                        0 ) AS saleAmount, 
                
                (0) AS saleAmountDue, 
                
                'Afs' AS saleCurrency,  
                
                coalesce(
                    (   
                        SELECT SaleItem.saleItemDueDate FROM SaleItem LEFT JOIN Sale on Sale.saleUid = 
                        SaleItem.saleItemSaleUid WHERE SaleItem.saleItemSaleUid = sl.saleUid  
                        AND CAST(Sale.saleActive AS INTEGER) = 1  
                        AND CAST(SaleItem.saleItemPreOrder AS INTEGER) = 1 
                        ORDER BY SaleItem.saleItemDueDate ASC LIMIT 1 
                    ) 
                , 0) AS earliestDueDate,
                     
                (SELECT count(*) FROM SaleItem WHERE SaleItem.saleItemSaleUid = sl.saleUid) AS saleItemCount,
                    COALESCE((SELECT SUM(SalePayment.salePaymentPaidAmount) FROM SalePayment  
                    WHERE SalePayment.salePaymentSaleUid = sl.saleUid 
                    AND CAST(SalePayment.salePaymentDone AS INTEGER) = 1 
                    AND CAST(SalePayment.salePaymentActive AS INTEGER) = 1 ) ,0) 
                AS saleAmountPaid,
                 
                (select (case  when  
                    (SELECT count(*) from SaleItem sip where sip.saleItemSaleUid = sl.saleUid 
                    AND CAST(sip.saleItemPreOrder AS INTEGER) = 1 ) > 0  then 1  else 0 end)  
                from Sale limit 1) AS saleItemPreOrder
                 
                FROM Sale sl 
                    LEFT JOIN Person AS Customer ON Customer.personUid = sl.saleCustomerUid
                    LEFT JOIN SaleItem ON SaleItem.saleItemSaleUid = sl.saleUid 
                        AND CAST(SaleItem.saleItemActive AS INTEGER) = 1
                    LEFT JOIN Person as LE ON LE.personUid = :leUid
                    LEFT JOIN Person as SalePerson ON SalePerson.personUid = sl.salePersonUid 
                
                WHERE CAST(sl.saleActive AS INTEGER) = 1
                
                AND (
					CAST(LE.admin AS INTEGER) = 1 OR 
					sl.salePersonUid = LE.personUid
					)

                AND             
                 (SELECT SaleItem.saleItemQuantity 
                  FROM Sale stg 
                  LEFT JOIN SaleItem ON SaleItem.saleItemSaleUid = stg.saleUid 
                    AND CAST(SaleItem.saleItemActive AS INTEGER) = 1  
                  WHERE stg.saleUid = sl.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1 
                  ORDER BY stg.saleCreationDate ASC LIMIT 1 
                  )  
                  || 'x ' || 
                  (SELECT case when Product.productName != '' 
                        then Product.productName 
                        else case when Product.productNameDari != '' 
                            then Product.productNameDari 
                            else case when Product.productNamePashto != '' 
                                then Product.productNamePashto 
                                else '' 
                                end  
                            end 
                        end  	
                  FROM SaleItem sitg 
                  LEFT JOIN Product ON Product.productUid = sitg.saleItemProductUid 
                  WHERE sitg.saleItemSaleUid = sl.saleUid AND CAST(sitg.saleItemActive AS INTEGER) = 1  
                  ORDER BY sitg.saleItemCreationDate ASC LIMIT 1) 
                  || 
                  (select 
                      (case  
                      when  
                      (SELECT count(*) from SaleItem sid where sid.saleItemSaleUid = sl.saleUid 
                                and CAST(sid.saleItemActive AS INTEGER) = 1 limit 1) > 1 
                      then '...'  
                      else '' 
                  end) 
                  from sale limit 1) LIKE :searchText
                
                AND CASE :filter WHEN $FILTER_PAYMENTS_DUE_SALES THEN 
                    (COALESCE( (SELECT SUM(SaleItem.saleItemPricePerPiece * SaleItem.saleItemQuantity) - 
                           SUM(Sale.saleDiscount)  FROM Sale LEFT JOIN SaleItem on SaleItem.saleItemSaleUid = 
                           Sale.saleUid AND CAST(SaleItem.saleItemActive AS INTEGER) = 1  
                            WHERE Sale.saleUid = sl.saleUid) ,0 
                          ) - COALESCE((SELECT SUM(SalePayment.salePaymentPaidAmount) FROM SalePayment 
                              WHERE SalePayment.salePaymentSaleUid = sl.saleUid 
                               AND CAST(SalePayment.salePaymentDone AS INTEGER) = 1 
                               AND CAST(SalePayment.salePaymentActive AS INTEGER) = 1 ) ,
                          0)
                    ) > 0
                    ELSE CAST(sl.saleActive AS INTEGER) = 1 END
                
                GROUP BY saleUid, Customer.personUid 
                
                ORDER BY saleLastUpdateDate DESC
        """

    }
}
