package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.core.db.dao.ContentEntryDaoCommon.SORT_TITLE_ASC
import com.ustadmobile.core.db.dao.ContentEntryDaoCommon.SORT_TITLE_DESC
import app.cash.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.composites.ContentEntryAndDetail
import com.ustadmobile.lib.db.composites.ContentEntryAndLanguage
import com.ustadmobile.lib.db.composites.ContentEntryAndListDetail
import com.ustadmobile.lib.db.entities.*

@DoorDao
@Repository
expect abstract class ContentEntryDao : BaseDao<ContentEntry> {

    @Insert
    abstract suspend fun insertListAsync(entityList: List<ContentEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(entity: ContentEntry)

    @Query("""
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

    @Query("""
        SELECT ContentEntry.*
          FROM ContentEntry
         WHERE ContentEntry.contentEntryUid = :entryUuid 
    """)
    abstract suspend fun findEntryWithContainerByEntryId(
        entryUuid: Long
    ): ContentEntry?

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES
    )
    @Query("""
            SELECT ContentEntry.*, ContentEntryVersion.*
              FROM ContentEntry
                   LEFT JOIN ContentEntryVersion
                             ON ContentEntryVersion.cevUid = 
                             (SELECT ContentEntryVersion.cevUid
                                FROM ContentEntryVersion
                               WHERE ContentEntryVersion.cevContentEntryUid = :entryUuid
                                 AND CAST(cevInActive AS INTEGER) = 0
                            ORDER BY ContentEntryVersion.cevLct DESC
                              LIMIT 1)
             WHERE ContentEntry.contentEntryUid = :entryUuid
            """)
    abstract fun findEntryWithContainerByEntryIdLive(
        entryUuid: Long
    ): Flow<ContentEntryAndDetail?>

    @Query("SELECT * FROM ContentEntry WHERE sourceUrl = :sourceUrl LIMIT 1")
    abstract fun findBySourceUrl(sourceUrl: String): ContentEntry?

    @HttpAccessible(
        clientStrategy = HttpAccessible.ClientStrategy.PULL_REPLICATE_ENTITIES,
        pullQueriesToReplicate = arrayOf(
            HttpServerFunctionCall(
                functionName = "findEntryWithContainerByEntryId",
            )
        )
    )
    @Query("SELECT title FROM ContentEntry WHERE contentEntryUid = :entryUuid")
    abstract suspend fun findTitleByUidAsync(entryUuid: Long): String?

    @Query("SELECT ContentEntry.* FROM ContentEntry LEFT Join ContentEntryParentChildJoin " +
            "ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid")
    abstract fun getChildrenByParentUid(parentUid: Long): PagingSource<Int, ContentEntry>

    @Query("""
        SELECT ContentEntry.*
          FROM ContentEntryParentChildJoin
               JOIN ContentEntry 
                    ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid
         WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid
    """)
    abstract suspend fun getChildrenByParentAsync(parentUid: Long): List<ContentEntry>

    @Query("SELECT COUNT(*) FROM ContentEntry LEFT Join ContentEntryParentChildJoin " +
            "ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid")
    abstract suspend fun getCountNumberOfChildrenByParentUUidAsync(parentUid: Long): Int


    @Query("SELECT * FROM ContentEntry where contentEntryUid = :parentUid LIMIT 1")
    abstract suspend fun getContentByUuidAsync(parentUid: Long): ContentEntry?


    @Query("SELECT ContentEntry.* FROM ContentEntry LEFT JOIN ContentEntryRelatedEntryJoin " +
            "ON ContentEntryRelatedEntryJoin.cerejRelatedEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryRelatedEntryJoin.relType = 1 AND ContentEntryRelatedEntryJoin.cerejRelatedEntryUid != :entryUuid")
    abstract suspend fun findAllLanguageRelatedEntriesAsync(entryUuid: Long): List<ContentEntry>

    @Repository(methodType = Repository.METHOD_DELEGATE_TO_WEB)
    @Query("SELECT DISTINCT ContentCategory.contentCategoryUid, ContentCategory.name AS categoryName, " +
            "ContentCategorySchema.contentCategorySchemaUid, ContentCategorySchema.schemaName FROM ContentEntry " +
            "LEFT JOIN ContentEntryContentCategoryJoin ON ContentEntryContentCategoryJoin.ceccjContentEntryUid = ContentEntry.contentEntryUid " +
            "LEFT JOIN ContentCategory ON ContentCategory.contentCategoryUid = ContentEntryContentCategoryJoin.ceccjContentCategoryUid " +
            "LEFT JOIN ContentCategorySchema ON ContentCategorySchema.contentCategorySchemaUid = ContentCategory.ctnCatContentCategorySchemaUid " +
            "LEFT JOIN ContentEntryParentChildJoin ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid " +
            "AND ContentCategory.contentCategoryUid != 0 ORDER BY ContentCategory.name")
    abstract suspend fun findListOfCategoriesAsync(parentUid: Long): List<DistinctCategorySchema>

    @Query("SELECT DISTINCT Language.* from Language " +
            "LEFT JOIN ContentEntry ON ContentEntry.primaryLanguageUid = Language.langUid " +
            "LEFT JOIN ContentEntryParentChildJoin ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid ORDER BY Language.name")
    abstract suspend fun findUniqueLanguagesInListAsync(parentUid: Long): List<Language>

    @Repository(methodType = Repository.METHOD_DELEGATE_TO_WEB)
    @Query("""SELECT DISTINCT Language.langUid, Language.name AS langName from Language
        LEFT JOIN ContentEntry ON ContentEntry.primaryLanguageUid = Language.langUid
        LEFT JOIN ContentEntryParentChildJoin ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid 
        WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid ORDER BY Language.name""")
    abstract suspend fun findUniqueLanguageWithParentUid(parentUid: Long): List<LangUidAndName>

    @Update
    abstract override fun update(entity: ContentEntry)


    @Query("SELECT * FROM ContentEntry WHERE contentEntryUid = :entryUid")
    abstract suspend fun findByUidAsync(entryUid: Long): ContentEntry?

    @Query("""
        SELECT ContentEntry.*, Language.*
          FROM ContentEntry
               LEFT JOIN Language 
                      ON Language.langUid = ContentEntry.primaryLanguageUid 
         WHERE ContentEntry.contentEntryUid = :uid              
    """)
    abstract suspend fun findByUidWithLanguageAsync(uid: Long): ContentEntryWithLanguage?


    @Query("SELECT * FROM ContentEntry WHERE contentEntryUid = :entryUid")
    abstract fun findByUid(entryUid: Long): ContentEntry?


    @Query("SELECT * FROM ContentEntry WHERE title = :title")
    abstract fun findByTitle(title: String): Flow<ContentEntry?>

    @Query("SELECT ContentEntry.* FROM ContentEntry " +
            "WHERE ContentEntry.sourceUrl = :sourceUrl")
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
        )
    )
    @Query("""
            SELECT ContentEntry.*, ContentEntryParentChildJoin.*
              FROM ContentEntry 
                    LEFT JOIN ContentEntryParentChildJoin 
                         ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid 
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
                     ContentEntry.contentEntryUid""")
    abstract fun getChildrenByParentUidWithCategoryFilterOrderByName(
        parentUid: Long,
        langParam: Long,
        categoryParam0: Long,
        sortOrder: Int,
        includeDeleted: Boolean,
    ): PagingSource<Int, ContentEntryAndListDetail>

    @Query("""
        SELECT ContentEntry.*, ContentEntryParentChildJoin.*
          FROM CourseBlock
               JOIN ContentEntry 
                    ON CourseBlock.cbType = ${CourseBlock.BLOCK_CONTENT_TYPE}
                       AND ContentEntry.contentEntryUid = CourseBlock.cbEntityUid
                       AND CAST(CourseBlock.cbActive AS INTEGER) = 1
               LEFT JOIN ContentEntryParentChildJoin
                         ON ContentEntryParentChildJoin.cepcjParentContentEntryUid = 0
         WHERE CourseBlock.cbClazzUid IN
               (SELECT ClazzEnrolment.clazzEnrolmentClazzUid
                  FROM ClazzEnrolment
                 WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :personUid)
    """)
    abstract fun getContentFromMyCourses(
        personUid: Long
    ): PagingSource<Int, ContentEntryAndListDetail>


    @Query("""
        SELECT ContentEntry.*, ContentEntryParentChildJoin.*
          FROM ContentEntry
               LEFT JOIN ContentEntryParentChildJoin
                         ON ContentEntryParentChildJoin.cepcjParentContentEntryUid = 0
         WHERE ContentEntry.contentOwner = :personUid
    """)
    abstract fun getContentByOwner(
        personUid: Long
    ): PagingSource<Int, ContentEntryAndListDetail>


    @Update
    abstract suspend fun updateAsync(entity: ContentEntry): Int

    @Query("SELECT ContentEntry.* FROM ContentEntry "+
            "LEFT JOIN ContentEntryParentChildJoin ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid" +
            " WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid")
    abstract fun getChildrenByAll(parentUid: Long): List<ContentEntry>


    @Query("SELECT * FROM ContentEntry where contentEntryUid = :parentUid LIMIT 1")
    abstract fun findLiveContentEntry(parentUid: Long): Flow<ContentEntry?>

    @Query("""SELECT COALESCE((SELECT contentEntryUid 
                                      FROM ContentEntry 
                                     WHERE entryId = :objectId 
                                     LIMIT 1),0) AS ID""")
    abstract fun getContentEntryUidFromXapiObjectId(objectId: String): Long


    @Query("SELECT * FROM ContentEntry WHERE sourceUrl LIKE :sourceUrl")
    abstract fun findSimilarIdEntryForKhan(sourceUrl: String): List<ContentEntry>

    /**
     * This query is used to tell the client how big a download job is, even if the client does
     * not yet have the indexes
     */
    @Repository(methodType = Repository.METHOD_DELEGATE_TO_WEB)
    @Query("""
        WITH RECURSIVE 
               ContentEntry_recursive(contentEntryUid, containerSize) AS (
               SELECT contentEntryUid, 
                            (SELECT COALESCE((SELECT fileSize 
                                           FROM Container 
                                          WHERE containerContentEntryUid = ContentEntry.contentEntryUid 
                                       ORDER BY cntLastModified DESC LIMIT 1), 0)) AS containerSize 
                 FROM ContentEntry 
                WHERE contentEntryUid = :contentEntryUid
                  AND NOT ceInactive
        UNION 
            SELECT ContentEntry.contentEntryUid, 
                (SELECT COALESCE((SELECT fileSize 
                                    FROM Container 
                                   WHERE containerContentEntryUid = ContentEntry.contentEntryUid 
                                ORDER BY cntLastModified DESC LIMIT 1), 0)) AS containerSize  
                  FROM ContentEntry
             LEFT JOIN ContentEntryParentChildJoin 
                    ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid,
                            ContentEntry_recursive
                  WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = ContentEntry_recursive.contentEntryUid
                    AND NOT ceInactive)
        SELECT COUNT(*) AS numEntries, 
               SUM(containerSize) AS totalSize 
          FROM ContentEntry_recursive""")
    abstract suspend fun getRecursiveDownloadTotals(contentEntryUid: Long): DownloadJobSizeInfo?

    @Query("""
            UPDATE ContentEntry 
               SET ceInactive = :ceInactive,
                   contentEntryLct = :changedTime        
            WHERE ContentEntry.contentEntryUid = :contentEntryUid""")
    abstract fun updateContentEntryInActive(
        contentEntryUid: Long,
        ceInactive: Boolean,
        changedTime: Long
    )

    @Query("""
        UPDATE ContentEntry 
           SET contentTypeFlag = :contentFlag,
               contentEntryLct = :changedTime 
         WHERE ContentEntry.contentEntryUid = :contentEntryUid""")
    abstract fun updateContentEntryContentFlag(
        contentFlag: Int,
        contentEntryUid: Long,
        changedTime: Long
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replaceList(entries: List<ContentEntry>)

    @Query("""Select ContentEntry.contentEntryUid AS uid, ContentEntry.title As labelName 
                    from ContentEntry WHERE contentEntryUid IN (:contentEntryUids)""")
    abstract suspend fun getContentEntryFromUids(contentEntryUids: List<Long>): List<UidAndLabel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertWithReplace(entry: ContentEntry)

    @Query("SELECT ContentEntry.*, Language.* FROM ContentEntry LEFT JOIN Language ON Language.langUid = ContentEntry.primaryLanguageUid")
    abstract fun findAllLive(): Flow<List<ContentEntryWithLanguage>>

    @Query("""
        UPDATE ContentEntry 
           SET ceInactive = :toggleVisibility, 
               contentEntryLct = :changedTime 
         WHERE contentEntryUid IN (:selectedItem)""")
    abstract suspend fun toggleVisibilityContentEntryItems(
        toggleVisibility: Boolean,
        selectedItem: List<Long>,
        changedTime: Long
    )

    @Query("""
SELECT ContentEntry.*
  FROM ContentEntry
       JOIN Container ON Container.containerUid = 
       (SELECT containerUid 
          FROM Container
         WHERE Container.containercontententryUid = ContentEntry.contentEntryUid
           AND Container.cntLastModified = 
               (SELECT MAX(ContainerInternal.cntLastModified)
                  FROM Container ContainerInternal
                 WHERE ContainerInternal.containercontententryUid = ContentEntry.contentEntryUid))
 WHERE ContentEntry.leaf 
   AND NOT ContentEntry.ceInactive
   AND (NOT EXISTS 
       (SELECT ContainerEntry.ceUid
          FROM ContainerEntry
         WHERE ContainerEntry.ceContainerUid = Container.containerUid)
        OR Container.fileSize = 0)   
    """)
    abstract suspend fun findContentEntriesWhereIsLeafAndLatestContainerHasNoEntriesOrHasZeroFileSize(): List<ContentEntry>

}
