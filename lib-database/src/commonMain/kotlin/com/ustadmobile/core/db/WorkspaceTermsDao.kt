package com.ustadmobile.core.db

import androidx.room.Dao
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

}