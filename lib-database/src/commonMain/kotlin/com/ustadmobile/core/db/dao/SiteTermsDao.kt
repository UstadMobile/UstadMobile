package com.ustadmobile.core.db.dao

import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.SiteTerms
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import com.ustadmobile.lib.db.entities.UserSession

@DoorDao
@Repository
expect abstract class SiteTermsDao : OneToManyJoinDao<SiteTerms> {

    @Query("""
     REPLACE INTO SiteTermsReplicate(stPk, stDestination)
      SELECT DISTINCT SiteTerms.sTermsUid AS stPk,
             :newNodeId AS stDestination
        FROM SiteTerms
       WHERE SiteTerms.sTermsLct != COALESCE(
             (SELECT stVersionId
                FROM SiteTermsReplicate
               WHERE stPk = SiteTerms.sTermsUid
                 AND stDestination = :newNodeId), 0) 
      /*psql ON CONFLICT(stPk, stDestination) DO UPDATE
             SET stPending = true
      */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([SiteTerms::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
 REPLACE INTO SiteTermsReplicate(stPk, stDestination)
  SELECT DISTINCT SiteTerms.sTermsUid AS stUid,
         UserSession.usClientNodeId AS stDestination
    FROM ChangeLog
         JOIN SiteTerms
             ON ChangeLog.chTableId = ${SiteTerms.TABLE_ID}
                AND ChangeLog.chEntityPk = SiteTerms.sTermsUid
         JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
   WHERE UserSession.usClientNodeId != (
         SELECT nodeClientId 
           FROM SyncNode
          LIMIT 1)
     AND SiteTerms.sTermsLct != COALESCE(
         (SELECT stVersionId
            FROM SiteTermsReplicate
           WHERE stPk = SiteTerms.sTermsUid
             AND stDestination = UserSession.usClientNodeId), 0)
 /*psql ON CONFLICT(stPk, stDestination) DO UPDATE
     SET stPending = true
  */               
 """)
    @ReplicationRunOnChange([SiteTerms::class])
    @ReplicationCheckPendingNotificationsFor([SiteTerms::class])
    abstract suspend fun replicateOnChange()

    @Query("""
        SELECT * FROM SiteTerms WHERE sTermsUid = coalesce(
            (SELECT sTermsUid FROM SiteTerms st_int WHERE st_int.sTermsLang = :langCode LIMIT 1),
            (SELECT sTermsUid FROM SiteTerms st_int WHERE st_int.sTermsLang = 'en' LIMIT 1),
            0)
    """)
    abstract suspend fun findSiteTerms(langCode: String): SiteTerms?

    @Insert
    abstract suspend fun insertAsync(siteTerms: SiteTerms): Long

    @Query("SELECT * FROM SiteTerms WHERE sTermsUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): SiteTerms?

    @Query("""SELECT SiteTerms.*, Language.* 
        FROM SiteTerms 
        LEFT JOIN Language ON SiteTerms.sTermsLangUid = Language.langUid
        WHERE CAST(sTermsActive AS INTEGER) = 1
    """)
    abstract fun findAllTermsAsFactory(): DataSourceFactory<Int, SiteTermsWithLanguage>

    @Query("""SELECT SiteTerms.*, Language.*
        FROM SiteTerms
        LEFT JOIN Language ON SiteTerms.sTermsLangUid = Language.langUid
        WHERE CAST(sTermsActive AS INTEGER) = 1
    """)
    abstract suspend fun findAllWithLanguageAsList(): List<SiteTermsWithLanguage>


    @Query("""
        UPDATE SiteTerms 
           SET sTermsActive = :active,
               sTermsLct = :changeTime
         WHERE sTermsUid = :sTermsUid
        """)
    abstract suspend fun updateActiveByUid(sTermsUid: Long, active: Boolean, changeTime: Long)

}