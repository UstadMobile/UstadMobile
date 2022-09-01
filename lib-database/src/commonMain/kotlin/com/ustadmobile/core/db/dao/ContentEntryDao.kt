package com.ustadmobile.core.db.dao

import androidx.room.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.dao.ContentEntryDaoCommon.ACTIVE_CONTENT_JOB_ITEMS_CTE_SQL
import com.ustadmobile.core.db.dao.ContentEntryDaoCommon.ALL_ENTRIES_RECURSIVE_SQL
import com.ustadmobile.core.db.dao.ContentEntryDaoCommon.ENTITY_PERSONS_WITH_PERMISSION
import com.ustadmobile.core.db.dao.ContentEntryDaoCommon.ENTRY_WITH_CONTAINER_QUERY
import com.ustadmobile.core.db.dao.ContentEntryDaoCommon.LATEST_DOWNLOADED_CONTAINER_CTE_SQL
import com.ustadmobile.core.db.dao.ContentEntryDaoCommon.PLUGIN_ID_DELETE
import com.ustadmobile.core.db.dao.ContentEntryDaoCommon.PLUGIN_ID_DOWNLOAD
import com.ustadmobile.core.db.dao.ContentEntryDaoCommon.SORT_TITLE_ASC
import com.ustadmobile.core.db.dao.ContentEntryDaoCommon.SORT_TITLE_DESC
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.lifecycle.LiveData
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.*
import kotlin.js.JsName

@DoorDao
@Repository
expect abstract class ContentEntryDao : BaseDao<ContentEntry> {

    @Query("""
        REPLACE INTO ContentEntryReplicate(cePk, ceDestination)
         SELECT DISTINCT contentEntryUid AS ceUid,
                :newNodeId AS siteDestination
           FROM ContentEntry
          WHERE ContentEntry.contentEntryLct != COALESCE(
                (SELECT ceVersionId
                   FROM ContentEntryReplicate
                  WHERE cePk = ContentEntry.contentEntryUid
                    AND ceDestination = :newNodeId), -1) 
         /*psql ON CONFLICT(cePk, ceDestination) DO UPDATE
                SET cePending = true
         */       
    """)
    @ReplicationRunOnNewNode
    @ReplicationCheckPendingNotificationsFor([ContentEntry::class])
    abstract suspend fun replicateOnNewNode(@NewNodeIdParam newNodeId: Long)

    @Query("""
        REPLACE INTO ContentEntryReplicate(cePk, ceDestination)
         SELECT DISTINCT ContentEntry.contentEntryUid AS cePk,
                UserSession.usClientNodeId AS siteDestination
           FROM ChangeLog
                JOIN ContentEntry
                    ON ChangeLog.chTableId = ${ContentEntry.TABLE_ID}
                       AND ChangeLog.chEntityPk = ContentEntry.contentEntryUid
                JOIN UserSession ON UserSession.usStatus = ${UserSession.STATUS_ACTIVE}
          WHERE UserSession.usClientNodeId != (
                SELECT nodeClientId 
                  FROM SyncNode
                 LIMIT 1)
            AND ContentEntry.contentEntryLct != COALESCE(
                (SELECT ceVersionId
                   FROM ContentEntryReplicate
                  WHERE cePk = ContentEntry.contentEntryUid
                    AND ceDestination = UserSession.usClientNodeId), 0)     
        /*psql ON CONFLICT(cePk, ceDestination) DO UPDATE
            SET cePending = true
         */               
    """)
    @ReplicationRunOnChange([ContentEntry::class])
    @ReplicationCheckPendingNotificationsFor([ContentEntry::class])
    abstract suspend fun replicateOnChange()

    @JsName("insertListAsync")
    @Insert
    abstract suspend fun insertListAsync(entityList: List<ContentEntry>)

    @Query("SELECT ContentEntry.*, Language.* FROM ContentEntry LEFT JOIN Language ON Language.langUid = ContentEntry.primaryLanguageUid " +
            "WHERE ContentEntry.contentEntryUid=:entryUuid"
    )
    @JsName("findEntryWithLanguageByEntryId")
    abstract suspend fun findEntryWithLanguageByEntryIdAsync(entryUuid: Long): ContentEntryWithLanguage?

    @Query("""
        SELECT ContentEntry.*, 
               Language.*,
               CourseBlock.*
          FROM ContentEntry
               LEFT JOIN Language 
               ON Language.langUid = ContentEntry.primaryLanguageUid 
               
               LEFT JOIN CourseBlock
               ON CourseBlock.cbType = ${CourseBlock.BLOCK_CONTENT_TYPE}
               AND CourseBlock.cbEntityUid = :entityUid
               
         WHERE ContentEntry.contentEntryUid = :entityUid       
    """)
    @JsName("findEntryWithBlockAndLanguageByUidAsync")
    abstract suspend fun findEntryWithBlockAndLanguageByUidAsync(entityUid: Long): ContentEntryWithBlockAndLanguage?

    @Query(ENTRY_WITH_CONTAINER_QUERY)
    abstract suspend fun findEntryWithContainerByEntryId(entryUuid: Long): ContentEntryWithMostRecentContainer?

    @Query(ENTRY_WITH_CONTAINER_QUERY)
    abstract fun findEntryWithContainerByEntryIdLive(entryUuid: Long): LiveData<ContentEntryWithMostRecentContainer?>

    @Query("SELECT * FROM ContentEntry WHERE sourceUrl = :sourceUrl LIMIT 1")
    @JsName("findBySourceUrl")
    abstract fun findBySourceUrl(sourceUrl: String): ContentEntry?

    @Query("SELECT title FROM ContentEntry WHERE contentEntryUid = :contentEntryUid")
    abstract suspend fun findTitleByUidAsync(contentEntryUid: Long): String?

    @Query("SELECT ContentEntry.* FROM ContentEntry LEFT Join ContentEntryParentChildJoin " +
            "ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid")
    @JsName("getChildrenByParentUid")
    abstract fun getChildrenByParentUid(parentUid: Long): DataSourceFactory<Int, ContentEntry>

    @Query("""
        SELECT ContentEntry.*
          FROM ContentEntryParentChildJoin
               JOIN ContentEntry 
                    ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid
         WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid
    """)
    abstract suspend fun getChildrenByParentAsync(parentUid: Long): List<ContentEntry>

    @Query("""
        SELECT ContentEntry.contentEntryUid AS contentEntryUid, ContentEntry.leaf AS leaf, 
               COALESCE(Container.containerUid, 0) AS mostRecentContainerUid,
               COALESCE(Container.fileSize, 0) AS mostRecentContainerSize
          FROM ContentEntryParentChildJoin
               JOIN ContentEntry 
                    ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid
               LEFT JOIN Container
                    ON containerUid = 
                        (SELECT COALESCE((
                                SELECT Container.containerUid 
                                  FROM Container
                                 WHERE Container.containerContentEntryUid = ContentEntry.contentEntryUid
                              ORDER BY Container.cntLastModified DESC
                                 LIMIT 1),0))
         WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid
         LIMIT :limit
        OFFSET :offset 
    """)
    abstract suspend fun getContentJobItemParamsByParentUid(
        parentUid: Long,
        limit: Int,
        offset: Int
    ): List<ContentEntryContentJobItemParams>

    @Query("SELECT COUNT(*) FROM ContentEntry LEFT Join ContentEntryParentChildJoin " +
            "ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid")
    @JsName("getCountNumberOfChildrenByParentUUidAsync")
    abstract suspend fun getCountNumberOfChildrenByParentUUidAsync(parentUid: Long): Int


    @Query("SELECT * FROM ContentEntry where contentEntryUid = :parentUid LIMIT 1")
    @JsName("getContentByUuidAsync")
    abstract suspend fun getContentByUuidAsync(parentUid: Long): ContentEntry?


    @Query("SELECT ContentEntry.* FROM ContentEntry LEFT JOIN ContentEntryRelatedEntryJoin " +
            "ON ContentEntryRelatedEntryJoin.cerejRelatedEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryRelatedEntryJoin.relType = 1 AND ContentEntryRelatedEntryJoin.cerejRelatedEntryUid != :entryUuid")
    @JsName("findAllLanguageRelatedEntriesAsync")
    abstract suspend fun findAllLanguageRelatedEntriesAsync(entryUuid: Long): List<ContentEntry>

    @Repository(methodType = Repository.METHOD_DELEGATE_TO_WEB)
    @RepoHttpAccessible
    @Query("SELECT DISTINCT ContentCategory.contentCategoryUid, ContentCategory.name AS categoryName, " +
            "ContentCategorySchema.contentCategorySchemaUid, ContentCategorySchema.schemaName FROM ContentEntry " +
            "LEFT JOIN ContentEntryContentCategoryJoin ON ContentEntryContentCategoryJoin.ceccjContentEntryUid = ContentEntry.contentEntryUid " +
            "LEFT JOIN ContentCategory ON ContentCategory.contentCategoryUid = ContentEntryContentCategoryJoin.ceccjContentCategoryUid " +
            "LEFT JOIN ContentCategorySchema ON ContentCategorySchema.contentCategorySchemaUid = ContentCategory.ctnCatContentCategorySchemaUid " +
            "LEFT JOIN ContentEntryParentChildJoin ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid " +
            "AND ContentCategory.contentCategoryUid != 0 ORDER BY ContentCategory.name")
    @JsName("findListOfCategoriesAsync")
    abstract suspend fun findListOfCategoriesAsync(parentUid: Long): List<DistinctCategorySchema>

    @Query("SELECT DISTINCT Language.* from Language " +
            "LEFT JOIN ContentEntry ON ContentEntry.primaryLanguageUid = Language.langUid " +
            "LEFT JOIN ContentEntryParentChildJoin ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid ORDER BY Language.name")
    @JsName("findUniqueLanguagesInListAsync")
    @RepoHttpAccessible
    abstract suspend fun findUniqueLanguagesInListAsync(parentUid: Long): List<Language>

    @Repository(methodType = Repository.METHOD_DELEGATE_TO_WEB)
    @Query("""SELECT DISTINCT Language.langUid, Language.name AS langName from Language
        LEFT JOIN ContentEntry ON ContentEntry.primaryLanguageUid = Language.langUid
        LEFT JOIN ContentEntryParentChildJoin ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid 
        WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid ORDER BY Language.name""")
    @JsName("findUniqueLanguageWithParentUid")
    @RepoHttpAccessible
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
    @JsName("findByUid")
    abstract fun findByUid(entryUid: Long): ContentEntry?


    @Query("SELECT * FROM ContentEntry WHERE title = :title")
    @JsName("findByTitle")
    abstract fun findByTitle(title: String): LiveData<ContentEntry?>

    /**
     * For new jobs, if the user is currently on Mobile Data, we will assume that they know what
     * they want to do, and by default, allow the use of mobile data.
     */
    @Query("""
       SELECT COALESCE((SELECT CAST(cjIsMeteredAllowed AS INTEGER) 
                FROM ContentJobItem 
                JOIN ContentJob
                    ON ContentJobItem.cjiJobUid = ContentJob.cjUid
               WHERE cjiContentEntryUid = :contentEntryUid
                AND cjiRecursiveStatus >= ${JobStatus.QUEUED}
                AND cjiRecursiveStatus <= ${JobStatus.RUNNING_MAX} LIMIT 1),
                CAST(((SELECT connectivityState
                        FROM ConnectivityStatus
                       LIMIT 1) = ${ConnectivityStatus.STATE_METERED}) AS INTEGER),
                0) AS Status
    """)
    abstract suspend fun isMeteredAllowedForEntry(contentEntryUid: Long): Boolean


    @Query("SELECT ContentEntry.* FROM ContentEntry " +
            "WHERE ContentEntry.sourceUrl = :sourceUrl")
    @JsName("findBySourceUrlWithContentEntryStatusAsync")
    abstract suspend fun findBySourceUrlWithContentEntryStatusAsync(sourceUrl: String): ContentEntry?

    @Query("""
            SELECT ContentEntry.*, ContentEntryParentChildJoin.*, Container.*, 
                COALESCE(StatementEntity.resultScoreMax,0) AS resultMax, 
                COALESCE(StatementEntity.resultScoreRaw,0) AS resultScore, 
                COALESCE(StatementEntity.resultScoreScaled,0) AS resultScaled, 
                COALESCE(StatementEntity.extensionProgress,0) AS progress, 
                COALESCE(StatementEntity.resultCompletion,'FALSE') AS contentComplete,
                COALESCE(StatementEntity.resultSuccess, 0) AS success,
                COALESCE((CASE WHEN StatementEntity.resultCompletion 
                THEN 1 ELSE 0 END),0) AS totalCompletedContent,
                0 AS assignmentContentWeight,
                
                1 as totalContent, 
                
                0 as penalty
            FROM ContentEntry 
                    LEFT JOIN ContentEntryParentChildJoin 
                    ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid 
                    
                    LEFT JOIN StatementEntity
							ON StatementEntity.statementUid = 
                                (SELECT statementUid 
							       FROM StatementEntity 
                                  WHERE statementContentEntryUid = ContentEntry.contentEntryUid 
							        AND StatementEntity.statementPersonUid = :personUid
							        AND contentEntryRoot 
                               ORDER BY resultScoreScaled DESC, extensionProgress DESC, resultSuccess DESC LIMIT 1)
                    
                    LEFT JOIN Container 
                    ON Container.containerUid = 
                        (SELECT containerUid 
                           FROM Container 
                          WHERE containerContentEntryUid = ContentEntry.contentEntryUid 
                       ORDER BY cntLastModified DESC LIMIT 1)
            WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid 
            AND (:langParam = 0 OR ContentEntry.primaryLanguageUid = :langParam) 
            AND (NOT ContentEntry.ceInactive OR ContentEntry.ceInactive = :showHidden) 
            AND (NOT ContentEntry.leaf OR NOT ContentEntry.leaf = :onlyFolder) 
            AND (ContentEntry.publik 
                 OR (SELECT username
                        FROM Person
                       WHERE personUid = :personUid) IS NOT NULL) 
            AND 
            (:categoryParam0 = 0 OR :categoryParam0 
                IN (SELECT ceccjContentCategoryUid 
                      FROM ContentEntryContentCategoryJoin 
                     WHERE ceccjContentEntryUid = ContentEntry.contentEntryUid)) 
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
    @JsName("getChildrenByParentUidWithCategoryFilterOrderByNameAsc")
    abstract fun getChildrenByParentUidWithCategoryFilterOrderByName(parentUid: Long, langParam: Long,
                                                                     categoryParam0: Long, personUid: Long,
                                                                     showHidden: Boolean, onlyFolder: Boolean,
                                                                     sortOrder: Int): DataSourceFactory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>





    @Query("""
        SELECT ContentEntry.*, ContentEntryParentChildJoin.*, Container.*, 
                COALESCE(StatementEntity.resultScoreMax,0) AS resultMax, 
                COALESCE(StatementEntity.resultScoreRaw,0) AS resultScore, 
                COALESCE(StatementEntity.resultScoreScaled,0) AS resultScaled, 
                COALESCE(StatementEntity.extensionProgress,0) AS progress, 
                COALESCE(StatementEntity.resultCompletion,'FALSE') AS contentComplete,
                COALESCE(StatementEntity.resultSuccess, 0) AS success,
                COALESCE((CASE WHEN StatementEntity.resultCompletion 
                THEN 1 ELSE 0 END),0) AS totalCompletedContent,
                0 AS assignmentContentWeight,
                
                1 as totalContent, 
                
                0 as penalty
          FROM CourseBlock
               JOIN ContentEntry 
                    ON CourseBlock.cbType = ${CourseBlock.BLOCK_CONTENT_TYPE}
                       AND ContentEntry.contentEntryUid = CourseBlock.cbEntityUid
                       AND CAST(CourseBlock.cbActive AS INTEGER) = 1
               LEFT JOIN ContentEntryParentChildJoin 
                    ON ContentEntryParentChildJoin.cepcjUid = 0 
               LEFT JOIN StatementEntity
							ON StatementEntity.statementUid = 
                                (SELECT statementUid 
							       FROM StatementEntity 
                                  WHERE statementContentEntryUid = ContentEntry.contentEntryUid 
							        AND StatementEntity.statementPersonUid = :personUid
							        AND contentEntryRoot 
                               ORDER BY resultScoreScaled DESC, extensionProgress DESC, resultSuccess DESC LIMIT 1)     
               LEFT JOIN Container 
                    ON Container.containerUid = 
                        (SELECT containerUid 
                           FROM Container 
                          WHERE containerContentEntryUid = ContentEntry.contentEntryUid 
                       ORDER BY cntLastModified DESC LIMIT 1)  
                               
         WHERE CourseBlock.cbClazzUid IN
               (SELECT ClazzEnrolment.clazzEnrolmentClazzUid
                  FROM ClazzEnrolment
                 WHERE ClazzEnrolment.clazzEnrolmentPersonUid = :personUid)
    """)
    abstract fun getContentFromMyCourses(
        personUid: Long
    ): DataSourceFactory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>


    @Query("""
        SELECT ContentEntry.*, ContentEntryParentChildJoin.*, Container.*, 
                COALESCE(StatementEntity.resultScoreMax,0) AS resultMax, 
                COALESCE(StatementEntity.resultScoreRaw,0) AS resultScore, 
                COALESCE(StatementEntity.resultScoreScaled,0) AS resultScaled, 
                COALESCE(StatementEntity.extensionProgress,0) AS progress, 
                COALESCE(StatementEntity.resultCompletion,'FALSE') AS contentComplete,
                COALESCE(StatementEntity.resultSuccess, 0) AS success,
                COALESCE((CASE WHEN StatementEntity.resultCompletion 
                THEN 1 ELSE 0 END),0) AS totalCompletedContent,
                0 AS assignmentContentWeight,
                
                1 as totalContent, 
                
                0 as penalty
          FROM ContentEntry
               LEFT JOIN ContentEntryParentChildJoin 
                    ON ContentEntryParentChildJoin.cepcjUid = 0 
               LEFT JOIN StatementEntity
							ON StatementEntity.statementUid = 
                                (SELECT statementUid 
							       FROM StatementEntity 
                                  WHERE statementContentEntryUid = ContentEntry.contentEntryUid 
							        AND StatementEntity.statementPersonUid = :personUid
							        AND contentEntryRoot 
                               ORDER BY resultScoreScaled DESC, extensionProgress DESC, resultSuccess DESC LIMIT 1)     
               LEFT JOIN Container 
                    ON Container.containerUid = 
                        (SELECT containerUid 
                           FROM Container 
                          WHERE containerContentEntryUid = ContentEntry.contentEntryUid 
                       ORDER BY cntLastModified DESC LIMIT 1)  
         WHERE ContentEntry.contentOwner = :personUid
           AND NOT EXISTS(
               SELECT ContentEntryParentChildJoin.cepcjUid 
                 FROM ContentEntryParentChildJoin
                WHERE ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid)
    """)
    abstract fun getContentByOwner(
        personUid: Long
    ): DataSourceFactory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>


    @Update
    abstract suspend fun updateAsync(entity: ContentEntry): Int

    @Query("SELECT ContentEntry.* FROM ContentEntry "+
            "LEFT JOIN ContentEntryParentChildJoin ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid" +
            " WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid")
    @JsName("getChildrenByAll")
    abstract fun getChildrenByAll(parentUid: Long): List<ContentEntry>


    @JsName("findLiveContentEntry")
    @Query("SELECT * FROM ContentEntry where contentEntryUid = :parentUid LIMIT 1")
    abstract fun findLiveContentEntry(parentUid: Long): LiveData<ContentEntry?>

    @Query("""SELECT COALESCE((SELECT contentEntryUid 
                                      FROM ContentEntry 
                                     WHERE entryId = :objectId 
                                     LIMIT 1),0) AS ID""")
    @JsName("getContentEntryUidFromXapiObjectId")
    abstract fun getContentEntryUidFromXapiObjectId(objectId: String): Long


    @Query("SELECT * FROM ContentEntry WHERE sourceUrl LIKE :sourceUrl")
    @JsName("findSimilarIdEntryForKhan")
    abstract fun findSimilarIdEntryForKhan(sourceUrl: String): List<ContentEntry>

    /**
     * This query is used to tell the client how big a download job is, even if the client does
     * not yet have the indexes
     */
    @Repository(methodType = Repository.METHOD_DELEGATE_TO_WEB)
    @RepoHttpAccessible
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

    @Query(ALL_ENTRIES_RECURSIVE_SQL)
    abstract fun getAllEntriesRecursively(contentEntryUid: Long): DataSourceFactory<Int, ContentEntryWithParentChildJoinAndMostRecentContainer>

    @Query(ALL_ENTRIES_RECURSIVE_SQL)
    abstract fun getAllEntriesRecursivelyAsList(contentEntryUid: Long): List<ContentEntryWithParentChildJoinAndMostRecentContainer>

    @Query("""
            UPDATE ContentEntry 
               SET ceInactive = :ceInactive,
                   contentEntryLct = :changedTime        
            WHERE ContentEntry.contentEntryUid = :contentEntryUid""")
    @JsName("updateContentEntryInActive")
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
    @JsName("updateContentEntryContentFlag")
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
    abstract fun findAllLive(): LiveData<List<ContentEntryWithLanguage>>

    /** Check if a permission is present on a specific entity e.g. updateState/modify etc */
    @Query("SELECT EXISTS(SELECT 1 FROM ContentEntry WHERE " +
            "ContentEntry.contentEntryUid = :contentEntryUid AND :accountPersonUid IN ($ENTITY_PERSONS_WITH_PERMISSION))")
    abstract suspend fun personHasPermissionWithContentEntry(accountPersonUid: Long, contentEntryUid: Long,
                                                      permission: Long) : Boolean


    @Query("""
        UPDATE ContentEntry
           SET ceInactive = :inactive,
               contentEntryLct = :changedTime
         WHERE contentEntryUid IN 
               (SELECT cjiContentEntryUid 
                  FROM ContentJobItem
                 WHERE cjiJobUid = :jobId
                   AND CAST(ContentJobItem.cjiContentDeletedOnCancellation AS INTEGER) = 1)
    """)
    abstract suspend fun updateContentEntryActiveByContentJobUid(
        jobId: Long,
        inactive: Boolean,
        changedTime: Long
    )


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

    //langauge=RoomSql
    @Query("""
        WITH ContentEntryContainerUids AS 
             (SELECT Container.containerUid
                FROM Container
               WHERE Container.containerContentEntryUid = :contentEntryUid
                   AND Container.fileSize > 0),
                   
             $LATEST_DOWNLOADED_CONTAINER_CTE_SQL,
                            
             $ACTIVE_CONTENT_JOB_ITEMS_CTE_SQL,
                  
            ShowDownload(showDownload) AS 
            (SELECT CAST(:platformDownloadEnabled AS INTEGER) = 1
                AND (SELECT containerUid FROM LatestDownloadedContainer) = 0
                AND (SELECT COUNT(*) FROM ActiveContentJobItems) = 0
                AND (SELECT COUNT(*) FROM ContentEntryContainerUids) > 0)
                   
        SELECT (SELECT showDownload FROM ShowDownload)
               AS showDownloadButton,
        
               CAST(:platformDownloadEnabled AS INTEGER) = 0
               OR (SELECT containerUid FROM LatestDownloadedContainer) != 0          
               AS showOpenButton,
       
               (SELECT NOT showDownload FROM ShowDownload)
           AND (SELECT COUNT(*) FROM ActiveContentJobItems) = 0    
           AND (SELECT COALESCE(
                       (SELECT cntLastModified
                          FROM Container
                         WHERE containerContentEntryUid = :contentEntryUid
                           AND fileSize > 0
                      ORDER BY cntLastModified DESC), 0)) 
               > (SELECT COALESCE(
                         (SELECT cntLastModified
                            FROM Container
                           WHERE Container.containerUid = 
                                 (SELECT LatestDownloadedContainer.containerUid
                                    FROM LatestDownloadedContainer)), 0)) 
               AS showUpdateButton,
               
               CAST(:platformDownloadEnabled AS INTEGER) = 1
           AND (SELECT containerUid FROM LatestDownloadedContainer) != 0
           AND (SELECT COUNT(*) FROM ActiveContentJobItems) = 0    
               AS showDeleteButton,
               
               (SELECT COUNT(*) 
                  FROM ActiveContentJobItems 
                 WHERE cjiPluginId = $PLUGIN_ID_DOWNLOAD) > 0
               AS showManageDownloadButton
    """)
    abstract suspend fun buttonsToShowForContentEntry(
        contentEntryUid: Long,
        platformDownloadEnabled: Boolean,
    ): ContentEntryButtonModel?

    @Query("""
        SELECT ContentJobItem.cjiRecursiveStatus AS status
         FROM ContentJobItem
        WHERE ContentJobItem.cjiContentEntryUid = :contentEntryUid
          AND ContentJobItem.cjiPluginId != $PLUGIN_ID_DELETE
          AND ContentJobItem.cjiStatus BETWEEN ${JobStatus.QUEUED} AND ${JobStatus.FAILED}
          AND NOT EXISTS(
              SELECT 1
                FROM ContentJobItem ContentJobItemInternal
               WHERE ContentJobItemInternal.cjiContentEntryUid = :contentEntryUid
                 AND ContentJobItemInternal.cjiPluginId = $PLUGIN_ID_DELETE
                 AND ContentJobItemInternal.cjiFinishTime > ContentJobItem.cjiStartTime)
     ORDER BY ContentJobItem.cjiFinishTime DESC
        LIMIT 1
    """)
    abstract suspend fun statusForDownloadDialog(
        contentEntryUid: Long
    ): Int

    @Query("""
        SELECT ContentJobItem.cjiRecursiveStatus AS status, 
               ContentJobItem.cjiRecursiveProgress AS progress,
               ContentJobItem.cjiRecursiveTotal AS total
         FROM ContentJobItem
        WHERE ContentJobItem.cjiContentEntryUid = :contentEntryUid
          AND ContentJobItem.cjiPluginId != $PLUGIN_ID_DELETE
          AND ContentJobItem.cjiStatus BETWEEN ${JobStatus.QUEUED} AND ${JobStatus.FAILED}
          AND NOT EXISTS(
              SELECT 1
                FROM ContentJobItem ContentJobItemInternal
               WHERE ContentJobItemInternal.cjiContentEntryUid = :contentEntryUid
                 AND ContentJobItemInternal.cjiPluginId = $PLUGIN_ID_DELETE
                 AND ContentJobItemInternal.cjiFinishTime > ContentJobItem.cjiStartTime)
     ORDER BY ContentJobItem.cjiFinishTime DESC
        LIMIT 1
    """)
    abstract suspend fun statusForContentEntryList(
        contentEntryUid: Long
    ): ContentJobItemProgressAndStatus?

}
