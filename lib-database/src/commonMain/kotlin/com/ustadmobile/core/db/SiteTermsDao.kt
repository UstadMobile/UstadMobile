package com.ustadmobile.core.db

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.ustadmobile.core.db.dao.OneToManyJoinDao
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.entities.SiteTerms
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage

@Dao
@Repository
abstract class SiteTermsDao : OneToManyJoinDao<SiteTerms> {

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
    abstract fun findAllTermsAsFactory(): DataSource.Factory<Int, SiteTermsWithLanguage>

    @Query("""SELECT SiteTerms.*, Language.*
        FROM SiteTerms
        LEFT JOIN Language ON SiteTerms.sTermsLangUid = Language.langUid
        WHERE CAST(sTermsActive AS INTEGER) = 1
    """)
    abstract suspend fun findAllWithLanguageAsList(): List<SiteTermsWithLanguage>


    @Transaction
    override suspend fun deactivateByUids(uidList: List<Long>) {
        uidList.forEach {
            updateActiveByUid(it, false)
        }
    }

    @Query("""
        UPDATE SiteTerms 
        SET sTermsActive = :active,
        sTermsLastChangedBy = (SELECT nodeClientId FROM SyncNode LIMIT 1)
        WHERE sTermsUid = :sTermsUid
        """)
    abstract suspend fun updateActiveByUid(sTermsUid: Long, active: Boolean)

}