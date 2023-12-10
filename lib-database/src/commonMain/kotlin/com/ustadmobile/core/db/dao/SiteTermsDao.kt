package com.ustadmobile.core.db.dao

import com.ustadmobile.door.annotation.DoorDao
import androidx.room.Insert
import androidx.room.Query
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.SiteTerms
import com.ustadmobile.lib.db.entities.SiteTermsWithLanguage
import kotlinx.coroutines.flow.Flow

@DoorDao
@Repository
expect abstract class SiteTermsDao : OneToManyJoinDao<SiteTerms> {

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

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findAllTermsAsListFlow",
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        "activeOnly",
                        HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "0"
                    )
                )
            )
        )
    )
    @Query("""
        SELECT SiteTerms.*
          FROM SiteTerms
         WHERE :activeOnly = 0 
            OR CAST(sTermsActive AS INTEGER) = 1
    """)
    abstract fun findAllTermsAsListFlow(
        activeOnly: Int
    ): Flow<List<SiteTerms>>

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