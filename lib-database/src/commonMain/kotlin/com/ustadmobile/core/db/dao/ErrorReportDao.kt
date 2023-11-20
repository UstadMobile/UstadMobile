package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ErrorReport

@DoorDao
@Repository
expect abstract class ErrorReportDao {


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
         WHERE errorCode = :errCode  
    """)
    abstract suspend fun findByErrorCode(errCode: Int): List<ErrorReport>

}