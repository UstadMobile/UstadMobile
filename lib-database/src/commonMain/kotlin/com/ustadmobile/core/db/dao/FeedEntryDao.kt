package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.lib.db.entities.FeedEntry
import com.ustadmobile.lib.db.entities.FeedSummary

@Dao
abstract class FeedEntryDao {

    @Query("""
        SELECT FeedEntry.*
          FROM FeedEntry
         WHERE FeedEntry.fePersonUid = :personUid
    """)
    abstract fun findByPersonUidAsDataSource(personUid: Long): DataSource.Factory<Int, FeedEntry>

    @Query("""
        SELECT FeedEntry.*
          FROM FeedEntry
         WHERE FeedEntry.fePersonUid = :personUid
    """)
    abstract suspend fun findByPersonAsList(personUid: Long): List<FeedEntry>

    @Insert
    abstract fun insertList(entityList: List<FeedEntry>)

    @Insert
    abstract suspend fun insertListAsync(entityList: List<FeedEntry>)

    @Query("""
     SELECT 
			(SELECT COUNT(ClazzEnrolment.clazzEnrolmentUid) 
			   FROM ClazzEnrolment
			  WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :personUid) AS numClazzes,
			(SELECT COUNT(DISTINCT CASE WHEN StatementEntity.contentEntryRoot 
							            THEN StatementEntity.statementContentEntryUid
							            ELSE NULL
							            END)
			  FROM StatementEntity
			 WHERE StatementEntity.statementPersonUid = :personUid) AS numContentEntriesCompleted,
			(SELECT COALESCE(SUM(StatementEntity.resultDuration), 0)
			   FROM StatementEntity
			  WHERE StatementEntity.statementPersonUid = :personUid) AS contentUsageMillis
    """)
    abstract fun getFeedSummary(personUid: Long): FeedSummary?


}