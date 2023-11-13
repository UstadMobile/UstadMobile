package com.ustadmobile.core.db.dao

import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ContextXObjectStatementJoin

@DoorDao
@Repository
expect abstract class ContextXObjectStatementJoinDao : BaseDao<ContextXObjectStatementJoin> {


    @Query("SELECT * FROM ContextXObjectStatementJoin where contextStatementUid = :statementUid and contextXObjectUid = :objectUid")
    abstract fun findByStatementAndObjectUid(statementUid: Long, objectUid: Long): ContextXObjectStatementJoin?


}
