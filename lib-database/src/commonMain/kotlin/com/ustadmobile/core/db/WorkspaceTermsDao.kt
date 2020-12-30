package com.ustadmobile.core.db

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.WorkspaceTerms

@Dao
@Repository
abstract class WorkspaceTermsDao {

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

}