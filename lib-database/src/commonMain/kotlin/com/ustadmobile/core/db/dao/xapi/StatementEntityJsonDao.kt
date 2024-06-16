package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.xapi.StatementEntityJson

@DoorDao
@Repository
expect abstract class StatementEntityJsonDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertOrIgnoreListAsync(entityList: List<StatementEntityJson>)

    @Query("""
        SELECT StatementEntityJson.*
          FROM StatementEntityJson
         WHERE (    (:stmtJsonIdHi = 0 AND :stmtJsonIdLo = 0) 
                 OR (stmtJsonIdHi = :stmtJsonIdHi AND stmtJsonIdLo = :stmtJsonIdLo))
                  
    """)
    abstract suspend fun getStatements(
        stmtJsonIdHi: Long,
        stmtJsonIdLo: Long,
    ): List<StatementEntityJson>

}