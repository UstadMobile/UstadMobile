package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.School

@UmDao
@UmRepository
@Dao
abstract class SchoolDao : BaseDao<School> {

    @Query("SELECT * FROM School WHERE schoolName=:schoolName AND CAST(schoolActive AS INTEGER) = 1")
    abstract fun findByNameAsync(schoolName: String): School?

    @Query("SELECT * FROM School WHERE schoolUid = :schoolUid AND CAST(schoolActive AS INTEGER) = 1")
    abstract fun findByUidAsync(schoolUid: Long): School?

    @Query("SELECT * FROM School WHERE CAST(schoolActive AS INTEGER) = 1 " +
            " AND schoolName LIKE :searchBit ORDER BY schoolName ASC")
    abstract fun findAllActiveSchoolsNameAsc(searchBit: String): DataSource.Factory<Int, School>

    fun findAllSchoolsAndSort(searchBit: String, sortCode: Int): DataSource.Factory<Int, School>{
        //TODO:
        return findAllActiveSchoolsNameAsc(searchBit)
    }

    //TODO:
//    @Query("""""")
//    abstract fun checkPermission(permission: Long, personUid: Long): Boolean

    @Update
    abstract suspend fun updateAsync(entity: School): Int

}
