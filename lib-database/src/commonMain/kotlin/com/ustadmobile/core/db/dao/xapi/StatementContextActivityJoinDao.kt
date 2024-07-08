package com.ustadmobile.core.db.dao.xapi

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.xapi.StatementContextActivityJoin

@DoorDao
@Repository
expect abstract class StatementContextActivityJoinDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertOrIgnoreListAsync(entities: List<StatementContextActivityJoin>)

    @Query("""
        SELECT StatementContextActivityJoin.*
          FROM StatementContextActivityJoin
         WHERE StatementContextActivityJoin.scajFromStatementIdHi = :statementIdHi
           AND StatementContextActivityJoin.scajFromStatementIdLo = :statementIdLo
           AND StatementContextActivityJoin.scajContextType = :scajContextType
    """)
    abstract suspend fun findAllByStatementId(
        statementIdHi: Long,
        statementIdLo: Long,
        scajContextType: Int,
    ): List<StatementContextActivityJoin>

}