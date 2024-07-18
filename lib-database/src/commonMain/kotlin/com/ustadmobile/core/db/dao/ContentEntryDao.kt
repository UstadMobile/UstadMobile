package com.ustadmobile.core.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.cash.paging.PagingSource
import com.ustadmobile.core.db.dao.ContentEntryDaoCommon.SELECT_ACCOUNT_PERSON_AND_STATUS_FIELDS
import com.ustadmobile.core.db.dao.ContentEntryDaoCommon.SELECT_STATUS_FIELDS_FOR_CONTENT_ENTRY
import com.ustadmobile.core.db.dao.ContentEntryDaoCommon.SORT_TITLE_ASC
import com.ustadmobile.core.db.dao.ContentEntryDaoCommon.SORT_TITLE_DESC
import com.ustadmobile.core.db.dao.xapi.StatementDao
import com.ustadmobile.door.annotation.DoorDao
import com.ustadmobile.door.annotation.HttpAccessible
import com.ustadmobile.door.annotation.HttpServerFunctionCall
import com.ustadmobile.door.annotation.HttpServerFunctionParam
import com.ustadmobile.door.annotation.QueryLiveTables
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.db.composites.ContentEntryAndDetail
import com.ustadmobile.lib.db.composites.ContentEntryAndLanguage
import com.ustadmobile.lib.db.composites.ContentEntryAndListDetail
import com.ustadmobile.lib.db.composites.ContentEntryAndPicture
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.CourseBlock
import com.ustadmobile.lib.db.entities.UidAndLabel
import kotlinx.coroutines.flow.Flow

@DoorDao
@Repository
expect abstract class ContentEntryDao : BaseDao<ContentEntry> {

    @Insert
    abstract suspend fun insertListAsync(entityList: List<ContentEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(entity: ContentEntry)

    @Query("SELECT * FROM ContentEntry WHERE contentEntryUid = :entryUid")
    abstract suspend fun findByUidAsync(entryUid: Long): ContentEntry?

    @Query(
        """
        SELECT ContentEntry.*, Language.* 
          FROM ContentEntry 
               LEFT JOIN Language 
                         ON Language.langUid = ContentEntry.primaryLanguageUid
         WHERE ContentEntry.contentEntryUid=:entryUuid
        """
    )
    abstract suspend fun findEntryWithLanguageByEntryIdAsync(
        entryUuid: Long
    ): ContentEntryAndLanguage?

    @HttpAccessible
    @Query(
        """
        SELECT ContentEntry.*, ContentEntryPicture2.*
          FROM ContentEntry
               LEFT JOIN ContentEntryPicture2 
                         ON ContentEntryPicture2.cepUid = :uid
         WHERE ContentEntry.contentEntryUid = :uid                
    """
    )
    abstract suspend fun findByUidWithEditDetails(
        uid: Long,
    ): ContentEntryAndPicture?


    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall("findByContentEntryUidWithDetailsAsFlow"),
            HttpServerFunctionCall(
                functionName = "findStatusStatementsByContentEntryUid",
                functionDao = StatementDao::class,
            )
        )
    )
    @Query(
        """
              -- When the user is viewing ContentEntryDetail where the class is specified eg 
              -- for a ContentEntry that is part of a Clazz then results information will only be
              -- included if the user is a student in the class
              -- If the user is viewing the ContentEntryDetail via the library then the results
              -- information will always be included
              WITH IncludeResults(includeResults) AS (
                   SELECT CAST(
                      (SELECT (:clazzUid = 0)
                           OR (${ClazzEnrolmentDaoCommon.SELECT_ACCOUNT_PERSON_UID_IS_STUDENT_IN_CLAZZ_UID})
                      ) AS INTEGER)
                  )

              SELECT ContentEntry.*, ContentEntryVersion.*, ContentEntryPicture2.*,
                   :accountPersonUid AS sPersonUid,
                   :courseBlockUid AS sCbUid,
                   $SELECT_STATUS_FIELDS_FOR_CONTENT_ENTRY
              FROM ContentEntry
                   LEFT JOIN ContentEntryVersion
                             ON ContentEntryVersion.cevUid = 
                             (SELECT ContentEntryVersion.cevUid
                                FROM ContentEntryVersion
                               WHERE ContentEntryVersion.cevContentEntryUid = :contentEntryUid
                                 AND CAST(cevInActive AS INTEGER) = 0
                            ORDER BY ContentEntryVersion.cevLct DESC
                              LIMIT 1)
                   LEFT JOIN ContentEntryPicture2
                             ON ContentEntryPicture2.cepUid = :contentEntryUid   
             WHERE ContentEntry.contentEntryUid = :contentEntryUid
            """
    )
    @QueryLiveTables(
        arrayOf(
            "ContentEntry", "ContentEntryVersion", "ContentEntryPicture2", "CourseBlock",
            "ClazzEnrolment", "StatementEntity"
        )
    )
    abstract fun findByContentEntryUidWithDetailsAsFlow(
        contentEntryUid: Long,
        clazzUid: Long,
        courseBlockUid: Long,
        accountPersonUid: Long,
    ): Flow<ContentEntryAndDetail?>

    @Query("SELECT * FROM ContentEntry WHERE sourceUrl = :sourceUrl LIMIT 1")
    abstract fun findBySourceUrl(sourceUrl: String): ContentEntry?

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findByUidAsync",
            )
        )
    )
    @Query("SELECT title FROM ContentEntry WHERE contentEntryUid = :entryUid")
    abstract suspend fun findTitleByUidAsync(entryUid: Long): String?

    @Query(
        "SELECT ContentEntry.* FROM ContentEntry LEFT Join ContentEntryParentChildJoin " +
                "ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
                "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid"
    )
    abstract fun getChildrenByParentUid(parentUid: Long): PagingSource<Int, ContentEntry>

    @Query(
        """
        SELECT ContentEntry.*
          FROM ContentEntryParentChildJoin
               JOIN ContentEntry 
                    ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid
         WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid
    """
    )
    abstract suspend fun getChildrenByParentAsync(parentUid: Long): List<ContentEntry>

    @Query(
        "SELECT COUNT(*) FROM ContentEntry LEFT Join ContentEntryParentChildJoin " +
                "ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
                "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid"
    )
    abstract suspend fun getCountNumberOfChildrenByParentUUidAsync(parentUid: Long): Int


    @Query("SELECT * FROM ContentEntry where contentEntryUid = :parentUid LIMIT 1")
    abstract suspend fun getContentByUuidAsync(parentUid: Long): ContentEntry?


    @Query(
        "SELECT ContentEntry.* FROM ContentEntry LEFT JOIN ContentEntryRelatedEntryJoin " +
                "ON ContentEntryRelatedEntryJoin.cerejRelatedEntryUid = ContentEntry.contentEntryUid " +
                "WHERE ContentEntryRelatedEntryJoin.relType = 1 AND ContentEntryRelatedEntryJoin.cerejRelatedEntryUid != :entryUuid"
    )
    abstract suspend fun findAllLanguageRelatedEntriesAsync(entryUuid: Long): List<ContentEntry>

    @Update
    abstract override fun update(entity: ContentEntry)


    @Query(
        """
        SELECT ContentEntry.*, Language.*
          FROM ContentEntry
               LEFT JOIN Language 
                      ON Language.langUid = ContentEntry.primaryLanguageUid 
         WHERE ContentEntry.contentEntryUid = :uid              
    """
    )
    abstract suspend fun findByUidWithLanguageAsync(uid: Long): ContentEntryWithLanguage?


    @Query("SELECT * FROM ContentEntry WHERE contentEntryUid = :entryUid")
    abstract fun findByUid(entryUid: Long): ContentEntry?


    @Query("SELECT * FROM ContentEntry WHERE title = :title")
    abstract fun findByTitle(title: String): Flow<ContentEntry?>

    @Query(
        "SELECT ContentEntry.* FROM ContentEntry " +
                "WHERE ContentEntry.sourceUrl = :sourceUrl"
    )
    abstract suspend fun findBySourceUrlWithContentEntryStatusAsync(sourceUrl: String): ContentEntry?

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "getChildrenByParentUidWithCategoryFilterOrderByName",
                functionArgs = arrayOf(
                    HttpServerFunctionParam(
                        name = "includeDeleted",
                        argType = HttpServerFunctionParam.ArgType.LITERAL,
                        literalValue = "true",
                    )
                )
            ),
            HttpServerFunctionCall(
                functionName = "findListOfChildsByParentUuid",
                functionDao = ContentEntryParentChildJoinDao::class,
            ),
            HttpServerFunctionCall(
                functionName = "findStatusStatementByParentContentEntryUid",
                functionDao = StatementDao::class,
            )
        )
    )
    @Query(
        """
            WITH IncludeResults(includeResults) AS (SELECT 1)
            
            SELECT ContentEntry.*, ContentEntryParentChildJoin.*, ContentEntryPicture2.*,
                   $SELECT_ACCOUNT_PERSON_AND_STATUS_FIELDS
              FROM ContentEntry 
                    LEFT JOIN ContentEntryParentChildJoin 
                         ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid 
                    LEFT JOIN ContentEntryPicture2
                         ON ContentEntryPicture2.cepUid = ContentEntry.contentEntryUid
             WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid 
               AND (:langParam = 0 OR ContentEntry.primaryLanguageUid = :langParam)
               AND (:categoryParam0 = 0 OR :categoryParam0 
                    IN (SELECT ceccjContentCategoryUid 
                          FROM ContentEntryContentCategoryJoin 
                         WHERE ceccjContentEntryUid = ContentEntry.contentEntryUid)) 
               AND (CAST(:includeDeleted AS INTEGER) = 1 OR CAST(ContentEntryParentChildJoin.cepcjDeleted AS INTEGER) = 0)          
            ORDER BY ContentEntryParentChildJoin.childIndex,
                     CASE(:sortOrder)
                     WHEN $SORT_TITLE_ASC THEN ContentEntry.title
                     ELSE ''
                     END ASC,
                     CASE(:sortOrder)
                     WHEN $SORT_TITLE_DESC THEN ContentEntry.title
                     ELSE ''
                     END DESC,             
                     ContentEntry.contentEntryUid"""
    )
    abstract fun getChildrenByParentUidWithCategoryFilterOrderByName(
        accountPersonUid: Long,
        parentUid: Long,
        langParam: Long,
        categoryParam0: Long,
        sortOrder: Int,
        includeDeleted: Boolean,
    ): PagingSource<Int, ContentEntryAndListDetail>

    @Query(
        """
        WITH IncludeResults(includeResults) AS (SELECT 1)
        
        SELECT ContentEntry.*, ContentEntryParentChildJoin.*, ContentEntryPicture2.*,
               $SELECT_ACCOUNT_PERSON_AND_STATUS_FIELDS
          FROM CourseBlock
               JOIN ContentEntry 
                    ON CourseBlock.cbType = ${CourseBlock.BLOCK_CONTENT_TYPE}
                       AND ContentEntry.contentEntryUid = CourseBlock.cbEntityUid
                       AND CAST(CourseBlock.cbActive AS INTEGER) = 1
               LEFT JOIN ContentEntryParentChildJoin
                         ON ContentEntryParentChildJoin.cepcjParentContentEntryUid = 0
               LEFT JOIN ContentEntryPicture2
                         ON ContentEntryPicture2.cepUid = ContentEntry.contentEntryUid          
         WHERE CourseBlock.cbClazzUid IN
               (SELECT ClazzEnrolment.clazzEnrolmentClazzUid
                  FROM ClazzEnrolment
                 WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :accountPersonUid)
    """
    )
    abstract fun getContentFromMyCourses(
        accountPersonUid: Long
    ): PagingSource<Int, ContentEntryAndListDetail>


    @Query(
        """
        WITH IncludeResults(includeResults) AS (SELECT 1)
        
        SELECT ContentEntry.*, ContentEntryParentChildJoin.*, ContentEntryPicture2.*, 
               $SELECT_ACCOUNT_PERSON_AND_STATUS_FIELDS
          FROM ContentEntry
               LEFT JOIN ContentEntryParentChildJoin
                         ON ContentEntryParentChildJoin.cepcjParentContentEntryUid = 0
               LEFT JOIN ContentEntryPicture2
                         ON ContentEntryPicture2.cepUid = ContentEntry.contentEntryUid
         WHERE ContentEntry.contentOwner = :accountPersonUid
    """
    )
    abstract fun getContentByOwner(
        accountPersonUid: Long
    ): PagingSource<Int, ContentEntryAndListDetail>


    @Update
    abstract suspend fun updateAsync(entity: ContentEntry): Int

    @Query(
        "SELECT ContentEntry.* FROM ContentEntry " +
                "LEFT JOIN ContentEntryParentChildJoin ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid" +
                " WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid"
    )
    abstract fun getChildrenByAll(parentUid: Long): List<ContentEntry>


    @Query("SELECT * FROM ContentEntry where contentEntryUid = :parentUid LIMIT 1")
    abstract fun findLiveContentEntry(parentUid: Long): Flow<ContentEntry?>

    @Query(
        """SELECT COALESCE((SELECT contentEntryUid 
                                      FROM ContentEntry 
                                     WHERE entryId = :objectId 
                                     LIMIT 1),0) AS ID"""
    )
    abstract fun getContentEntryUidFromXapiObjectId(objectId: String): Long


    @Query("SELECT * FROM ContentEntry WHERE sourceUrl LIKE :sourceUrl")
    abstract fun findSimilarIdEntryForKhan(sourceUrl: String): List<ContentEntry>

    @Query(
        """
            UPDATE ContentEntry 
               SET ceInactive = :ceInactive,
                   contentEntryLct = :changedTime        
            WHERE ContentEntry.contentEntryUid = :contentEntryUid"""
    )
    abstract fun updateContentEntryInActive(
        contentEntryUid: Long,
        ceInactive: Boolean,
        changedTime: Long
    )

    @Query(
        """
        UPDATE ContentEntry 
           SET contentTypeFlag = :contentFlag,
               contentEntryLct = :changedTime 
         WHERE ContentEntry.contentEntryUid = :contentEntryUid"""
    )
    abstract fun updateContentEntryContentFlag(
        contentFlag: Int,
        contentEntryUid: Long,
        changedTime: Long
    )

    @Query(
        """Select ContentEntry.contentEntryUid AS uid, ContentEntry.title As labelName 
                    from ContentEntry WHERE contentEntryUid IN (:contentEntryUids)"""
    )
    abstract suspend fun getContentEntryFromUids(contentEntryUids: List<Long>): List<UidAndLabel>

    @Query(
        """
    WITH RECURSIVE ContentEntryRecursive AS (
        SELECT ce.contentEntryUid, ce.title, ce.leaf, ce.ceInactive
        FROM ContentEntry ce
        WHERE ce.contentEntryUid = :parentUid 
        AND ce.ceInactive = 0 
        AND ce.leaf = 0
        
        UNION ALL
        
        SELECT ce2.contentEntryUid, ce2.title, ce2.leaf, ce2.ceInactive
        FROM ContentEntryRecursive cr
        JOIN ContentEntryParentChildJoin cj ON cj.cepcjParentContentEntryUid = cr.contentEntryUid
        JOIN ContentEntry ce2 ON ce2.contentEntryUid = cj.cepcjChildContentEntryUid
        WHERE ce2.ceInactive = 0
    )
    SELECT *
    FROM ContentEntryRecursive
    ORDER BY title
"""
    )
    abstract suspend fun getRecursiveContentEntriesForExport(parentUid: Long): List<ContentEntry>


}
