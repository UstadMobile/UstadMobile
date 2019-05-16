package com.ustadmobile.core.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmQuery
import com.ustadmobile.lib.database.annotation.UmQueryFindByPrimaryKey
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.*

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@Dao
@UmRepository
abstract class ContentEntryDao : SyncableDao<ContentEntry, ContentEntryDao> {

    @Query("SELECT * FROM ContentEntry")
    abstract fun allEntries(): List<ContentEntry>

    @UmQuery("SELECT * FROM ContentEntry WHERE publik")
    abstract fun publicContentEntries(): List<ContentEntry>


    @UmQuery("SELECT DISTINCT ContentEntry.*, ContentEntryStatus.*, " +
            "(SELECT containerUid FROM Container " +
            "WHERE containerContentEntryUid =  ContentEntry.contentEntryUid ORDER BY lastModified DESC LIMIT 1) as mostRecentContainer " +
            "FROM DownloadJob \n" +
            "LEFT JOIN ContentEntry on  DownloadJob.djRootContentEntryUid = ContentEntry.contentEntryUid\n" +
            "LEFT JOIN ContentEntryStatus ON ContentEntryStatus.cesUid = ContentEntry.contentEntryUid \n ")
    abstract fun downloadedRootItems(): UmProvider<ContentEntryWithStatusAndMostRecentContainerUid>

    @Insert
    abstract fun insert(contentEntries: List<ContentEntry>): Array<Long>

    @Query("SELECT * FROM ContentEntry WHERE contentEntryUid=:entryUuid")
    abstract fun findByEntryId(entryUuid: Long): ContentEntry?

    @Query("SELECT * FROM ContentEntry WHERE sourceUrl = :sourceUrl LIMIT 1")
    abstract fun findBySourceUrl(sourceUrl: String): ContentEntry?

    @Query("SELECT ContentEntry.* FROM ContentEntry LEFT Join ContentEntryParentChildJoin " +
            "ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid")
    abstract fun getChildrenByParentUid(parentUid: Long): UmProvider<ContentEntry>

    @Query("SELECT ContentEntry.* FROM ContentEntry LEFT Join ContentEntryParentChildJoin " +
            "ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid")
    abstract fun getChildrenByParent(parentUid: Long): List<ContentEntry>

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

    @Update
    abstract override fun update(entity: ContentEntry)

    @UmQueryFindByPrimaryKey
    abstract suspend fun findByUidAsync(entryUid: Long): ContentEntry?

    @Query("SELECT ContentEntry.*, ContentEntryStatus.* FROM ContentEntry " +
            "LEFT JOIN ContentEntryStatus ON ContentEntryStatus.cesUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntry.contentEntryUid = :contentEntryUid")
    abstract suspend fun findByUidWithContentEntryStatusAsync(contentEntryUid: Long): ContentEntryWithContentEntryStatus?

    @Query("SELECT ContentEntry.*, ContentEntryStatus.* FROM ContentEntry " +
            "LEFT JOIN ContentEntryStatus ON ContentEntryStatus.cesUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntry.sourceUrl = :sourceUrl")
    abstract suspend fun findBySourceUrlWithContentEntryStatusAsync(sourceUrl: String): ContentEntryWithContentEntryStatus?

    @Query("SELECT ContentEntry.*,ContentEntryStatus.*, " +
            "(SELECT containerUid FROM Container " +
            "WHERE containerContentEntryUid =  ContentEntry.contentEntryUid ORDER BY lastModified DESC LIMIT 1) as mostRecentContainer " +
            "FROM ContentEntry " +
            "LEFT JOIN ContentEntryParentChildJoin ON ContentEntryParentChildJoin.cepcjChildContentEntryUid = ContentEntry.contentEntryUid " +
            "LEFT JOIN ContentEntryStatus ON ContentEntryStatus.cesUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntryParentChildJoin.cepcjParentContentEntryUid = :parentUid " +
            "AND " +
            "(:langParam = 0 OR ContentEntry.primaryLanguageUid = :langParam) " +
            "AND " +
            "(:categoryParam0 = 0 OR :categoryParam0 IN (SELECT ceccjContentCategoryUid FROM ContentEntryContentCategoryJoin " +
            "WHERE ceccjContentEntryUid = ContentEntry.contentEntryUid))")
    abstract fun getChildrenByParentUidWithCategoryFilter(parentUid: Long, langParam: Long, categoryParam0: Long): UmProvider<ContentEntryWithStatusAndMostRecentContainerUid>


}
