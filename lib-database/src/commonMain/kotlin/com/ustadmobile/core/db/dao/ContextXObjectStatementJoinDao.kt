package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.ContextXObjectStatementJoin

@Dao
@Repository
abstract class ContextXObjectStatementJoinDao : BaseDao<ContextXObjectStatementJoin> {

    @Query("SELECT * FROM ContextXObjectStatementJoin where contextStatementUid = :statementUid and contextXObjectUid = :objectUid")
    abstract fun findByStatementAndObjectUid(statementUid: Long, objectUid: Long): ContextXObjectStatementJoin?

    companion object {

        const val CONTEXT_FLAG_PARENT = 0

        const val CONTEXT_FLAG_CATEGORY = 1

        const val CONTEXT_FLAG_GROUPING = 2

        const val CONTEXT_FLAG_OTHER = 3
    }

}
