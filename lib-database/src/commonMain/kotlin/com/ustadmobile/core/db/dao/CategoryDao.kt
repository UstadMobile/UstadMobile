package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.ustadmobile.core.db.dao.ProductDao.Companion.SELECT_ACCOUNT_IS_ADMIN
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.Category
import com.ustadmobile.door.annotation.Repository

@Repository
@Dao
abstract class CategoryDao : BaseDao<Category> {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertOrReplace(entity: Category)

    @Update
    abstract suspend fun updateAsync(entity: Category): Int

    @Query("""
        SELECT * FROM Category WHERE CAST(Category.categoryActive AS INTEGER) = 1
    """)
    abstract fun findAllActiveCategories(): DataSource.Factory<Int, Category>


    @Query("""
        SELECT * FROM Category WHERE categoryUid = :uid 
         AND CAST(categoryActive AS INTEGER) = 1
    """)
    abstract suspend fun findByUidAsync(uid: Long): Category?

    companion object {

        const val SELECT_ACCOUNT_IS_ADMIN = "(SELECT admin FROM Person WHERE personUid = :accountPersonUid)"


    }
}
