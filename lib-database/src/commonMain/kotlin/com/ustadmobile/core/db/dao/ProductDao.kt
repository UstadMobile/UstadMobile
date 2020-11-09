package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.ustadmobile.core.db.dao.ProductDao.Companion.SELECT_ACCOUNT_IS_ADMIN
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Category
import com.ustadmobile.lib.db.entities.Product
import com.ustadmobile.lib.db.entities.ProductWithInventoryCount

@UmDao(updatePermissionCondition = SELECT_ACCOUNT_IS_ADMIN,
        insertPermissionCondition = SELECT_ACCOUNT_IS_ADMIN)
@UmRepository
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
    abstract suspend fun findProductWithInventoryCountAsync(uid: Long): ProductWithInventoryCount?

    @Query(QUERY_PRODUCTS_WITH_INVENTORY)
    abstract fun findAllActiveProductWithInventoryCount(): DataSource.Factory<Int, ProductWithInventoryCount>

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

    @Query(QUERY_FIND_ALL_CATEGORY_BY_PRODUCT)
    abstract suspend  fun findAllCategoriesOfProductUidAsync(productUid: Long): List<Category>


    companion object {

        const val SORT_NAME_ASC = 1

        const val SORT_NAME_DESC = 2

        const val SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)"

        const val FIND_BY_UID_QUERY = "SELECT * FROM Product WHERE productUid = :uid " +
                " AND CAST(productActive AS INTEGER) = 1"

        const val FINDWITHCOUNT_BY_UID_QUERY = """
                SELECT Product.* , 0 as stock
                    FROM Product WHERE Product.productUid = :uid 
                 AND CAST(productActive AS INTEGER) = 1
                 """

        const val FIND_BY_NAME_QUERY = "SELECT * FROM Product WHERE productName = :name AND CAST(productActive AS INTEGER) = 1"

        const val QUERY_PRODUCTS_WITH_INVENTORY = """
            SELECT Product.*, 
                (0) as stock
            FROM Product 
            WHERE CAST(productActive AS INTEGER) = 1 
        """


        const val QUERY_FIND_ALL_CATEGORY_BY_PRODUCT = """
            SELECT Category.* FROM ProductCategoryJoin
            LEFT JOIN Category ON Category.categoryUid = ProductCategoryJoin.productCategoryJoinCategoryUid
            WHERE 
            ProductCategoryJoin.productCategoryJoinProductUid = :productUid
            AND CAST(productCategoryJoinActive AS INTEGER ) = 1 
            ORDER BY ProductCategoryJoin.productCategoryJoinDateCreated DESC
        """

    }
}
