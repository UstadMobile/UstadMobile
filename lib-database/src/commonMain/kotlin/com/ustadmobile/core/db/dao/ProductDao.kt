package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Category
import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.lib.db.entities.UidAndLabel
import com.ustadmobile.lib.db.entities.ProductWithInventoryCount
import com.ustadmobile.door.annotation.Repository

@Repository
@Dao
abstract class ProductDao : BaseDao<Product> {

    @Query(FIND_BY_NAME_QUERY)
    abstract suspend fun findByName(name: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrReplace(entity: Product)

    @Update
    abstract suspend fun updateAsync(entity: Product): Int

    @Query(FIND_BY_NAME_QUERY)
    abstract fun findByNameSync(name: String): Product?

    @Query("SELECT * FROM Product WHERE CAST(productActive AS INTEGER) = 1 ")
    abstract fun findAllActiveProducts(): DataSource.Factory<Int, Product>

    @Query(FINDWITHCOUNT_BY_UID_QUERY)
    abstract suspend fun findProductWithInventoryCountAsync(uid: Long, leUid: Long): ProductWithInventoryCount?

    @Query(QUERY_PRODUCTS_WITH_INVENTORY)
    abstract fun findAllActiveProductWithInventoryCount(leUid: Long, searchText: String? = "%",
                categoryUid: Long)
            : DataSource.Factory<Int, ProductWithInventoryCount>

    @Query("""SELECT * FROM Product 
        WHERE CAST(productActive AS INTEGER) = 1
         AND Product.productName LIKE :searchText
        ORDER BY CASE(:sortOrder)
                WHEN ${SORT_NAME_ASC} THEN Product.productName
                ELSE ''
            END ASC,
            CASE(:sortOrder)
                WHEN ${SORT_NAME_DESC} THEN Product.productName
                ELSE ''
            END DESC
    """)
    abstract fun findAllActiveProductsSorted(sortOrder: Int, searchText: String): DataSource.Factory<Int, Product>

    @Query("SELECT * FROM Product WHERE CAST(productActive AS INTEGER) = 1 ")
    abstract fun findAllActiveRolesLive(): DoorLiveData<List<Product>>

    @Query(FIND_BY_UID_QUERY)
    abstract suspend fun findByUidAsync(uid: Long): Product?

    @Query(FIND_BY_UID_QUERY)
    abstract fun findByUidLive(uid: Long): DoorLiveData<Product?>

    @Query(QUERY_FIND_ALL_CATEGORY_BY_PRODUCT)
    abstract fun findAllCategoriesOfProductUid(productUid: Long): DataSource.Factory<Int, Category>

    @Query("""
        SELECT * From Product WHERE productUid = :productUid
    """)
    abstract fun findAllProductPictures(productUid: Long): DataSource.Factory<Int, Product>

    @Query(QUERY_FIND_ALL_CATEGORY_BY_PRODUCT)
    abstract suspend  fun findAllCategoriesOfProductUidAsync(productUid: Long): List<Category>


    @Query(QUERY_FIND_ALL_CATEGORY_BY_LE)
    abstract suspend fun findAllCategoriesByLeUidAsync(leUid: Long): List<Category>

    @Query("""SELECT Product.productUid AS uid, Product.productName As labelName 
                    FROM Product WHERE productUid IN (:productList)""")
    abstract suspend fun getProductsFromUids(productList: List<Long>): List<UidAndLabel>


    companion object {

        const val SORT_NAME_ASC = 1

        const val SORT_NAME_DESC = 2

        const val SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)"

        const val FIND_BY_UID_QUERY = "SELECT * FROM Product WHERE productUid = :uid " +
                " AND CAST(productActive AS INTEGER) = 1"

        const val FINDWITHCOUNT_BY_UID_QUERY = """
                SELECT Product.* ,
                 (
                SELECT 
                    CASE WHEN CAST(SUM(InventoryItem.inventoryItemQuantity) AS INTEGER) > 0 
                        THEN SUM(InventoryItem.inventoryItemQuantity) 
                        ELSE 0 
                    END
                FROM InventoryItem WHERE
                InventoryItem.inventoryItemProductUid = Product.productUid
                AND (CAST(LE.admin AS INTEGER) = 1 OR InventoryItem.inventoryItemLeUid = LE.personUid)
                AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1
                ) as stock
                    FROM Product
                    LEFT JOIN PERSON AS LE ON LE.personUid = :leUid
                    WHERE Product.productUid = :uid 
                 AND CAST(productActive AS INTEGER) = 1
                 """

        const val FIND_BY_NAME_QUERY = "SELECT * FROM Product WHERE productName = :name AND CAST(productActive AS INTEGER) = 1"

        const val QUERY_PRODUCTS_WITH_INVENTORY = """
            SELECT Product.*, 
                (
                SELECT CASE WHEN 
                CAST(SUM(InventoryItem.inventoryItemQuantity) AS INTEGER) > 0 
                THEN SUM(InventoryItem.inventoryItemQuantity) ELSE 0 END
                FROM InventoryItem WHERE
                InventoryItem.inventoryItemProductUid = Product.productUid
                AND (CAST(LE.admin AS INTEGER) = 1 OR InventoryItem.inventoryItemLeUid = LE.personUid)
                AND CAST(InventoryItem.inventoryItemActive AS INTEGER) = 1
                ) as stock
            FROM Product 
            LEFT JOIN PERSON AS LE ON LE.personUid = :leUid
            WHERE CAST(productActive AS INTEGER) = 1
             
             AND (lower(Product.productName) like :searchText OR
                     lower(Product.productNameDari) like :searchText OR 
                    lower(Product.productNamePashto) like :searchText )
             
            AND (Product.productPersonAdded = LE.personUid OR CAST(LE.admin AS INTEGER) = 1)
            
            AND (:categoryUid = 0 OR Product.productUid IN 
                    (SELECT productCategoryJoinProductUid FROM ProductCategoryJoin
                    WHERE
                    productCategoryJoinCategoryUid = :categoryUid
                    AND CAST(productCategoryJoinActive AS INTEGER) = 1
                    )
                )
        """


        const val QUERY_FIND_ALL_CATEGORY_BY_PRODUCT = """
            SELECT Category.* FROM ProductCategoryJoin
            LEFT JOIN Category ON Category.categoryUid = ProductCategoryJoin.productCategoryJoinCategoryUid
            WHERE 
            ProductCategoryJoin.productCategoryJoinProductUid = :productUid
            AND CAST(productCategoryJoinActive AS INTEGER ) = 1 
            GROUP BY Category.categoryUid
            ORDER BY Category.categoryDateAdded DESC
        """



        const val QUERY_FIND_ALL_CATEGORY_BY_LE = """
            SELECT Category.* FROM ProductCategoryJoin
            LEFT JOIN Category ON Category.categoryUid = ProductCategoryJoin.productCategoryJoinCategoryUid
            LEFT JOIN Person AS LE ON LE.personUid = :leUid
            WHERE 
            ProductCategoryJoin.productCategoryJoinProductUid IN 
    
            (SELECT Product.productUid FROM Product WHERE 
                Product.productPersonAdded = LE.personUid OR CAST(LE.admin AS INTEGER) = 1
            )
            
            
            AND CAST(productCategoryJoinActive AS INTEGER ) = 1
             GROUP BY Category.categoryUid
             ORDER BY Category.categoryDateAdded DESC
        """


    }
}
