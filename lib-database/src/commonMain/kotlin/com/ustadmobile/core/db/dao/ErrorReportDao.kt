package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ErrorReport

@Dao
@Repository
abstract class ErrorReportDao {

    @Insert
    abstract suspend fun insertAsync(errorReport: ErrorReport): Long

    @Query("""
        SELECT ErrorReport.* 
          FROM ErrorReport
         WHERE errUid = :errUid
    """)
    abstract suspend fun findByUidAsync(errUid: Long): ErrorReport?

    @Query("""
        SELECT ErrorReport.*
          FROM ErrorReport
         WHERE errorNum = :errCode  
    """)
    abstract suspend fun findByErrorCode(errCode: Int): List<ErrorReport>

}