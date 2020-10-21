package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.*
import kotlin.js.JsName

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@Dao
@UmRepository
abstract class ContentEntryDao : BaseDao<ContentEntry> {

    @JsName("insertListAsync")
    @Insert
    abstract suspend fun insertListAsync(entityList: List<ContentEntry>)

    @Query("""SELECT DISTINCT ContentEntry.*, ContentEntryStatus.*, Container.*, ContentEntryProgress.*, 
            0 AS cepcjUid, 0 as cepcjChildContentEntryUid, 0 AS cepcjParentContentEntryUid, 0 as childIndex, 0 AS cepcjLocalChangeSeqNum, 0 AS cepcjMasterChangeSeqNum, 0 AS cepcjLastChangedBy
            FROM DownloadJob 
            LEFT JOIN ContentEntry on  DownloadJob.djRootContentEntryUid = ContentEntry.contentEntryUid 
            LEFT JOIN ContentEntryProgress ON ContentEntryProgress.contentEntryProgressContentEntryUid = ContentEntry.contentEntryUid 
               AND ContentEntryProgress.contentEntryProgressPersonUid = :personUid AND ContentEntryProgress.contentEntryProgressActive 
            LEFT JOIN ContentEntryStatus ON ContentEntryStatus.cesUid = ContentEntry.contentEntryUid 
            LEFT JOIN Container ON Container.containerUid = (SELECT containerUid FROM Container 
            WHERE containerContentEntryUid =  ContentEntry.contentEntryUid ORDER BY cntLastModified DESC LIMIT 1) WHERE DownloadJob.djStatus < ${JobStatus.CANCELED} ORDER BY ContentEntry.title ASC""")
    @JsName("downloadedRootItemsAsc")
    abstract fun downloadedRootItemsAsc(personUid: Long): DataSource.Factory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>


    @Query("""SELECT DISTINCT ContentEntry.*, ContentEntryStatus.*, Container.*, ContentEntryProgress.*, 
            0 AS cepcjUid, 0 as cepcjChildContentEntryUid, 0 AS cepcjParentContentEntryUid, 0 as childIndex, 0 AS cepcjLocalChangeSeqNum, 0 AS cepcjMasterChangeSeqNum, 0 AS cepcjLastChangedBy 
            FROM DownloadJob 
            LEFT JOIN ContentEntry on  DownloadJob.djRootContentEntryUid = ContentEntry.contentEntryUid 
            LEFT JOIN ContentEntryProgress ON ContentEntryProgress.contentEntryProgressContentEntryUid = ContentEntry.contentEntryUid 
                AND ContentEntryProgress.contentEntryProgressPersonUid = :personUid AND ContentEntryProgress.contentEntryProgressActive 
            LEFT JOIN ContentEntryStatus ON ContentEntryStatus.cesUid = ContentEntry.contentEntryUid 
            LEFT JOIN Container ON Container.containerUid = (SELECT containerUid FROM Container 
            WHERE containerContentEntryUid =  ContentEntry.contentEntryUid ORDER BY cntLastModified DESC LIMIT 1) WHERE DownloadJob.djStatus < ${JobStatus.CANCELED} ORDER BY ContentEntry.title DESC""")
    @JsName("downloadedRootItemsDesc")
    abstract fun downloadedRootItemsDesc(personUid: Long): DataSource.Factory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>


    @Query("""SELECT DISTINCT ContentEntry.*, Container.*, ContentEntryProgress.*, 
            0 AS cepcjUid, 0 as cepcjChildContentEntryUid, 0 AS cepcjParentContentEntryUid, 0 as childIndex, 0 AS cepcjLocalChangeSeqNum, 0 AS cepcjMasterChangeSeqNum, 0 AS cepcjLastChangedBy
            FROM ContentEntry
            LEFT JOIN ContentEntryProgress ON ContentEntryProgress.contentEntryProgressContentEntryUid = ContentEntry.contentEntryUid 
                AND ContentEntryProgress.contentEntryProgressPersonUid = :personUid AND ContentEntryProgress.contentEntryProgressActive 
            LEFT JOIN Container ON Container.containerUid = (SELECT containerUid FROM Container 
            WHERE containerContentEntryUid =  ContentEntry.contentEntryUid ORDER BY cntLastModified DESC LIMIT 1) WHERE ContentEntry.ceInactive = :ceInactive """)
    @JsName("recycledItems")
    abstract fun recycledItems(ceInactive:Boolean = true, personUid: Long): DataSource.Factory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>


    @Query("SELECT ContentEntry.*, Language.* FROM ContentEntry LEFT JOIN Language ON Language.langUid = ContentEntry.primaryLanguageUid " +
            "WHERE ContentEntry.contentEntryUid=:entryUuid"
    )
    @JsName("findEntryWithLanguageByEntryId")
    abstract suspend fun findEntryWithLanguageByEntryIdAsync(entryUuid: Long): ContentEntryWithLanguage?

    @Query("SELECT ContentEntry.*, Container.* FROM ContentEntry LEFT JOIN Container ON Container.containerUid = (SELECT containerUid FROM Container "+
            "WHERE containerContentEntryUid =  ContentEntry.contentEntryUid ORDER BY cntLastModified DESC LIMIT 1) WHERE ContentEntry.contentEntryUid=:entryUuid")
    @JsName("findByEntryIdWithContainer")
    abstract suspend fun findEntryWithContainerByEntryId(entryUuid: Long): ContentEntryWithMostRecentContainer?

    @Query("SELECT * FROM ContentEntry WHERE sourceUrl = :sourceUrl LIMIT 1")
    @JsName("findBySourceUrl")
    abstract fun findBySourceUrl(sourceUrl: String): ContentEntry?

    @Query("SELECT title FROM ContentEntry WHERE contentEntryUid = :contentEntryUid")
    abstract suspend fun findTitleByUidAsync(contentEntryUid: Long): String?

    @Query("SELECT ContentEntry.* FROM ContentEntry LEFT Join ContentEntryParentChildJoin " +
            "ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid")
    @JsName("getChildrenByParentUid")
    abstract fun getChildrenByParentUid(parentUid: Long): DataSource.Factory<Int, ContentEntry>

    @Query("SELECT ContentEntry.* FROM ContentEntry LEFT Join ContentEntryParentChildJoin " +
            "ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid")
    @JsName("getChildrenByParentAsync")
    abstract suspend fun getChildrenByParentAsync(parentUid: Long): List<ContentEntry>

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
    abstract suspend fun findUniqueLanguagesInListAsync(parentUid: Long): List<Language>

    @Repository(methodType = Repository.METHOD_DELEGATE_TO_WEB)
    @Query("""SELECT DISTINCT Language.langUid, Language.name AS langName from Language
        LEFT JOIN ContentEntry ON ContentEntry.primaryLanguageUid = Language.langUid
        LEFT JOIN ContentEntryParentChildJoin ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid 
        WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid ORDER BY Language.name""")
    @JsName("findUniqueLanguageWithParentUid")
    abstract suspend fun findUniqueLanguageWithParentUid(parentUid: Long): List<LangUidAndName>

    @Update
    abstract override fun update(entity: ContentEntry)


    @Query("SELECT * FROM ContentEntry WHERE contentEntryUid = :entryUid")
    @JsName("findByUidAsync")
    abstract suspend fun findByUidAsync(entryUid: Long): ContentEntry?


    @Query("SELECT * FROM ContentEntry WHERE contentEntryUid = :entryUid")
    @JsName("findByUid")
    abstract fun findByUid(entryUid: Long): ContentEntry?


    @Query("SELECT ContentEntry.* FROM ContentEntry " +
            "WHERE ContentEntry.sourceUrl = :sourceUrl")
    @JsName("findBySourceUrlWithContentEntryStatusAsync")
    abstract suspend fun findBySourceUrlWithContentEntryStatusAsync(sourceUrl: String): ContentEntry?

    @Query("""SELECT ContentEntry.*,ContentEntryStatus.*, ContentEntryParentChildJoin.*, Container.*, ContentEntryProgress.* 
            FROM ContentEntry 
            LEFT JOIN ContentEntryParentChildJoin ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid 
            LEFT JOIN ContentEntryProgress ON ContentEntryProgress.contentEntryProgressContentEntryUid = ContentEntry.contentEntryUid 
                 AND ContentEntryProgress.contentEntryProgressPersonUid = :personUid AND ContentEntryProgress.contentEntryProgressActive 
            LEFT JOIN ContentEntryStatus ON ContentEntryStatus.cesUid = ContentEntry.contentEntryUid
            LEFT JOIN Container ON Container.containerUid = (SELECT containerUid FROM Container 
                WHERE containerContentEntryUid =  ContentEntry.contentEntryUid ORDER BY cntLastModified DESC LIMIT 1)
            WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid 
            AND 
            (:langParam = 0 OR ContentEntry.primaryLanguageUid = :langParam) 
            AND (NOT ContentEntry.ceInactive OR ContentEntry.ceInactive = :showHidden)
            AND (ContentEntry.publik OR :personUid != 0)
            AND 
            (:categoryParam0 = 0 OR :categoryParam0 IN (SELECT ceccjContentCategoryUid FROM ContentEntryContentCategoryJoin 
            WHERE ceccjContentEntryUid = ContentEntry.contentEntryUid)) ORDER BY ContentEntryParentChildJoin.childIndex, ContentEntry.title ASC , ContentEntry.contentEntryUid""")
    @JsName("getChildrenByParentUidWithCategoryFilterOrderByNameAsc")
    abstract fun getChildrenByParentUidWithCategoryFilterOrderByNameAsc(parentUid: Long, langParam: Long, categoryParam0: Long, personUid: Long, showHidden: Boolean): DataSource.Factory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>

    @Query("""SELECT ContentEntry.*,ContentEntryStatus.*, ContentEntryParentChildJoin.*, Container.*, ContentEntryProgress.* 
            FROM ContentEntry 
            LEFT JOIN ContentEntryParentChildJoin ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid 
            LEFT JOIN ContentEntryProgress ON ContentEntryProgress.contentEntryProgressContentEntryUid = ContentEntry.contentEntryUid 
                AND ContentEntryProgress.contentEntryProgressPersonUid = :personUid AND ContentEntryProgress.contentEntryProgressActive 
            LEFT JOIN ContentEntryStatus ON ContentEntryStatus.cesUid = ContentEntry.contentEntryUid
            LEFT JOIN Container ON Container.containerUid = (SELECT containerUid FROM Container 
                WHERE containerContentEntryUid =  ContentEntry.contentEntryUid ORDER BY cntLastModified DESC LIMIT 1)
            WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid 
            AND 
            (:langParam = 0 OR ContentEntry.primaryLanguageUid = :langParam) 
            AND (NOT ContentEntry.ceInactive OR ContentEntry.ceInactive = :showHidden)
            AND (ContentEntry.publik OR :personUid != 0)
            AND 
            (:categoryParam0 = 0 OR :categoryParam0 IN (SELECT ceccjContentCategoryUid FROM ContentEntryContentCategoryJoin 
            WHERE ceccjContentEntryUid = ContentEntry.contentEntryUid)) ORDER BY  ContentEntryParentChildJoin.childIndex, ContentEntry.title DESC, ContentEntry.contentEntryUid""")
    @JsName("getChildrenByParentUidWithCategoryFilterOrderByNameDesc")
    abstract fun getChildrenByParentUidWithCategoryFilterOrderByNameDesc(parentUid: Long, langParam: Long, categoryParam0: Long, personUid: Long, showHidden: Boolean): DataSource.Factory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer>


    @Update
    abstract suspend fun updateAsync(entity: ContentEntry): Int

    @Query("SELECT ContentEntry.* FROM ContentEntry "+
            "LEFT JOIN ContentEntryParentChildJoin ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid" +
            " WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid")
    @JsName("getChildrenByAll")
    abstract fun getChildrenByAll(parentUid: Long): List<ContentEntry>


    @JsName("findLiveContentEntry")
    @Query("SELECT * FROM ContentEntry where contentEntryUid = :parentUid LIMIT 1")
    abstract fun findLiveContentEntry(parentUid: Long): DoorLiveData<ContentEntry?>

    @Query("SELECT contentEntryUid FROM ContentEntry WHERE entryId = :objectId LIMIT 1")
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
    @Query("""WITH RECURSIVE ContentEntry_recursive(contentEntryUid, containerSize) AS (
    SELECT contentEntryUid, 
    (SELECT COALESCE((SELECT fileSize FROM Container WHERE containerContentEntryUid = ContentEntry.contentEntryUid ORDER BY cntLastModified DESC LIMIT 1), 0)) AS containerSize 
    FROM ContentEntry WHERE contentEntryUid = :contentEntryUid
    UNION 
    SELECT ContentEntry.contentEntryUid, (SELECT COALESCE((SELECT fileSize FROM Container WHERE containerContentEntryUid = ContentEntry.contentEntryUid ORDER BY cntLastModified DESC LIMIT 1), 0)) AS containerSize  FROM ContentEntry
    LEFT JOIN ContentEntryParentChildJoin ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid,
    ContentEntry_recursive
    WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = ContentEntry_recursive.contentEntryUid)
    SELECT COUNT(*) AS numEntries, SUM(containerSize) AS totalSize FROM ContentEntry_recursive""")
    abstract suspend fun getRecursiveDownloadTotals(contentEntryUid: Long): DownloadJobSizeInfo?

    @Query("""WITH RECURSIVE ContentEntry_recursive(
    contentEntryUid, title, ceInactive, contentFlags, description, entryId, author, publisher, licenseType, licenseName, licenseUrl, sourceUrl, thumbnailUrl, lastModified, primaryLanguageUid, languageVariantUid, leaf, publik, contentTypeFlag, contentEntryLocalChangeSeqNum, contentEntryMasterChangeSeqNum, contentEntryLastChangedBy,
    
    cepcjUid, cepcjChildContentEntryUid, cepcjParentContentEntryUid, childIndex, cepcjLocalChangeSeqNum, cepcjMasterChangeSeqNum, cepcjLastChangedBy,
    
    containerUid, cntLocalCsn, cntMasterCsn, cntLastModBy, fileSize, containerContentEntryUid, cntLastModified, mimeType, remarks, mobileOptimized, cntNumEntries
    ) AS (
    SELECT ContentEntry.contentEntryUid, ContentEntry.title, ContentEntry.ceInactive, ContentEntry.contentFlags, ContentEntry.description, ContentEntry.entryId, ContentEntry.author, ContentEntry.publisher, ContentEntry.licenseType, ContentEntry.licenseName, ContentEntry.licenseUrl, ContentEntry.sourceUrl, ContentEntry.thumbnailUrl, ContentEntry.lastModified, ContentEntry.primaryLanguageUid, ContentEntry.languageVariantUid, ContentEntry.leaf, ContentEntry.publik, ContentEntry.contentTypeFlag, ContentEntry.contentEntryLocalChangeSeqNum, ContentEntry.contentEntryMasterChangeSeqNum, ContentEntry.contentEntryLastChangedBy,
    ContentEntryParentChildJoin.cepcjUid, ContentEntryParentChildJoin.cepcjChildContentEntryUid, ContentEntryParentChildJoin.cepcjParentContentEntryUid, ContentEntryParentChildJoin.childIndex, ContentEntryParentChildJoin.cepcjLocalChangeSeqNum, ContentEntryParentChildJoin.cepcjMasterChangeSeqNum, ContentEntryParentChildJoin.cepcjLastChangedBy,
	Container.containerUid, Container.cntLocalCsn, Container.cntMasterCsn, Container.cntLastModBy, Container.fileSize, Container.containerContentEntryUid, Container.cntLastModified, Container.mimeType, Container.remarks, Container.mobileOptimized, Container.cntNumEntries
	FROM 
    ContentEntry
    LEFT JOIN ContentEntryParentChildJoin ON ContentEntry.contentEntryUid = ContentEntryParentChildJoin.cepcjChildContentEntryUid 
    LEFT JOIN Container ON Container.containerUid = (SELECT COALESCE((SELECT containerUid FROM Container WHERE containerContentEntryUid = ContentEntry.contentEntryUid ORDER BY cntLastModified DESC LIMIT 1), 0))
    WHERE ContentEntry.contentEntryUid = :contentEntryUid
    UNION
    SELECT ContentEntry.contentEntryUid, ContentEntry.title, ContentEntry.ceInactive, ContentEntry.contentFlags, ContentEntry.description, ContentEntry.entryId, ContentEntry.author, ContentEntry.publisher, ContentEntry.licenseType, ContentEntry.licenseName, ContentEntry.licenseUrl, ContentEntry.sourceUrl, ContentEntry.thumbnailUrl, ContentEntry.lastModified, ContentEntry.primaryLanguageUid, ContentEntry.languageVariantUid, ContentEntry.leaf, ContentEntry.publik, ContentEntry.contentTypeFlag, ContentEntry.contentEntryLocalChangeSeqNum, ContentEntry.contentEntryMasterChangeSeqNum, ContentEntry.contentEntryLastChangedBy,
    ContentEntryParentChildJoin.cepcjUid, ContentEntryParentChildJoin.cepcjChildContentEntryUid, ContentEntryParentChildJoin.cepcjParentContentEntryUid, ContentEntryParentChildJoin.childIndex, ContentEntryParentChildJoin.cepcjLocalChangeSeqNum, ContentEntryParentChildJoin.cepcjMasterChangeSeqNum, ContentEntryParentChildJoin.cepcjLastChangedBy, 
	Container.containerUid, Container.cntLocalCsn, Container.cntMasterCsn, Container.cntLastModBy, Container.fileSize, Container.containerContentEntryUid, Container.cntLastModified, Container.mimeType, Container.remarks, Container.mobileOptimized, Container.cntNumEntries
	FROM 
    ContentEntry
    LEFT JOIN ContentEntryParentChildJoin ON ContentEntry.contentEntryUid = ContentEntryParentChildJoin.cepcjChildContentEntryUid 
    LEFT JOIN Container ON Container.containerUid = (SELECT COALESCE((SELECT containerUid FROM Container WHERE containerContentEntryUid = ContentEntry.contentEntryUid ORDER BY cntLastModified DESC LIMIT 1), 0)),
    ContentEntry_recursive
    WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = ContentEntry_recursive.contentEntryUid)
    SELECT * FROM ContentEntry_recursive""")
    abstract fun getAllEntriesRecursively(contentEntryUid: Long): DataSource.Factory<Int, ContentEntryWithParentChildJoinAndMostRecentContainer>

    @Query("UPDATE ContentEntry SET ceInactive = :ceInactive WHERE ContentEntry.contentEntryUid = :contentEntryUid")
    @JsName("updateContentEntryInActive")
    abstract fun updateContentEntryInActive(contentEntryUid: Long, ceInactive: Boolean)

    @Query("UPDATE ContentEntry SET  contentTypeFlag = :contentFlag WHERE ContentEntry.contentEntryUid = :contentEntryUid")
    @JsName("updateContentEntryContentFlag")
    abstract fun updateContentEntryContentFlag(contentFlag: Int, contentEntryUid: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun replaceList(entries: List<ContentEntry>)

    @Query("""Select * from ContentEntry WHERE contentEntryUid IN (:contentEntryUids)""")
    abstract fun getContentEntryFromUids(contentEntryUids: List<Long>): List<ContentEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertWithReplace(entry: ContentEntry)

    @Query("SELECT ContentEntry.*, Language.* FROM ContentEntry LEFT JOIN Language ON Language.langUid = ContentEntry.primaryLanguageUid")
    abstract fun findAllLive(): DoorLiveData<List<ContentEntryWithLanguage>>

    /** Check if a permission is present on a specific entity e.g. updateState/modify etc */
    @Query("SELECT EXISTS(SELECT 1 FROM ContentEntry WHERE " +
            "ContentEntry.contentEntryUid = :contentEntryUid AND :accountPersonUid IN ($ENTITY_PERSONS_WITH_PERMISSION))")
    abstract suspend fun personHasPermissionWithContentEntry(accountPersonUid: Long, contentEntryUid: Long,
                                                      permission: Long) : Boolean

    companion object {

        const val ENTITY_PERSONS_WITH_PERMISSION_PT1 = """
            SELECT DISTINCT Person.PersonUid FROM Person
            LEFT JOIN PersonGroupMember ON Person.personUid = PersonGroupMember.groupMemberPersonUid
            LEFT JOIN EntityRole ON EntityRole.erGroupUid = PersonGroupMember.groupMemberGroupUid
            LEFT JOIN Role ON EntityRole.erRoleUid = Role.roleUid
            WHERE 
            CAST(Person.admin AS INTEGER) = 1
            OR 
            (EntityRole.ertableId = ${ContentEntry.TABLE_ID} AND 
            EntityRole.erEntityUid = ContentEntry.contentEntryUid AND
            (Role.rolePermissions &  
        """

        const val ENTITY_PERSONS_WITH_PERMISSION_PT2 = ") > 0)"

        const val ENTITY_PERSONS_WITH_PERMISSION = "$ENTITY_PERSONS_WITH_PERMISSION_PT1 :permission $ENTITY_PERSONS_WITH_PERMISSION_PT2"

    }
}
