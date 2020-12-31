package com.ustadmobile.core.db

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.ustadmobile.core.db.dao.OneToManyJoinDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.Schedule
import com.ustadmobile.lib.db.entities.WorkspaceTerms
import com.ustadmobile.lib.db.entities.WorkspaceTermsWithLanguage

@Dao
@Repository
abstract class WorkspaceTermsDao : OneToManyJoinDao<WorkspaceTerms> {

    @Query("""
        SELECT * FROM WorkspaceTerms WHERE wtUid = coalesce(
            (SELECT wtUid FROM WorkspaceTerms wt_int WHERE wt_int.wtLang = :langCode LIMIT 1),
            (SELECT wtUid FROM WorkspaceTerms wt_int WHERE wt_int.wtLang = 'en' LIMIT 1),
            0)
    """)
    abstract suspend fun findWorkspaceTerms(langCode: String): WorkspaceTerms?

    @Insert
    abstract suspend fun insertAsync(workspaceTerms: WorkspaceTerms): Long

    @Query("SELECT * FROM WorkspaceTerms WHERE wtUid = :uid")
    abstract suspend fun findByUidAsync(uid: Long): WorkspaceTerms?

    @Query("SELECT * FROM WorkspaceTerms")
    abstract fun findAllTermsAsFactory(): DataSource.Factory<Int, WorkspaceTerms>

    @Query("""SELECT WorkspaceTerms.*, Language.*
        FROM WorkspaceTerms
        LEFT JOIN Language ON WorkspaceTerms.wtLangUid = Language.langUid
        WHERE CAST(wtActive AS INTEGER) = 1
    """)
    abstract suspend fun findAllWithLanguageAsList(): List<WorkspaceTermsWithLanguage>


    @Transaction
    override suspend fun deactivateByUids(uidList: List<Long>) {
        uidList.forEach {
            updateActiveByUid(it, false)
        }
    }

    @Query("""
        UPDATE WorkspaceTerms 
        SET wtActive = :active,
        wtLastChangedBy = (SELECT nodeClientId FROM SyncNode LIMIT 1)
        WHERE wtUid = :wtUid
        """)
    abstract suspend fun updateActiveByUid(wtUid: Long, active: Boolean)

}