package com.ustadmobile.core.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.database.annotation.UmDao
import com.ustadmobile.lib.database.annotation.UmRepository
import com.ustadmobile.lib.db.entities.*
import kotlin.js.JsName

@UmDao(selectPermissionCondition = "(:accountPersonUid = :accountPersonUid)")
@Dao
@UmRepository
abstract class ContentEntryDao : BaseDao<ContentEntry> {

    @Query("SELECT * FROM ContentEntry")
    @JsName("allEntries")
    abstract fun allEntries(): List<ContentEntry>


    @Query("SELECT * FROM ContentEntry WHERE publik")
    @JsName("publicContentEntries")
    abstract fun publicContentEntries(): List<ContentEntry>

    @Query("SELECT DISTINCT ContentEntry.*, ContentEntryStatus.*, " +
            "(SELECT containerUid FROM Container " +
            "WHERE containerContentEntryUid =  ContentEntry.contentEntryUid ORDER BY lastModified DESC LIMIT 1) as mostRecentContainer " +
            "FROM DownloadJob \n" +
            "LEFT JOIN ContentEntry on  DownloadJob.djRootContentEntryUid = ContentEntry.contentEntryUid\n" +
            "LEFT JOIN ContentEntryStatus ON ContentEntryStatus.cesUid = ContentEntry.contentEntryUid \n ")
    @JsName("downloadedRootItems")
    abstract fun downloadedRootItems(): DataSource.Factory<Int, ContentEntryWithStatusAndMostRecentContainerUid>

    @Query("SELECT * FROM ContentEntry WHERE contentEntryUid=:entryUuid")
    @JsName("findByEntryId")
    abstract fun findByEntryId(entryUuid: Long): ContentEntry?

    @Query("SELECT * FROM ContentEntry WHERE sourceUrl = :sourceUrl LIMIT 1")
    @JsName("findBySourceUrl")
    abstract fun findBySourceUrl(sourceUrl: String): ContentEntry?

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

    @Update
    abstract override fun update(entity: ContentEntry)


    @Query("SELECT * FROM ContentEntry WHERE contentEntryUid = :entryUid")
    @JsName("findByUidAsync")
    abstract suspend fun findByUidAsync(entryUid: Long): ContentEntry?

    @Query("SELECT ContentEntry.*, ContentEntryStatus.* FROM ContentEntry " +
            "LEFT JOIN ContentEntryStatus ON ContentEntryStatus.cesUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntry.contentEntryUid = :contentEntryUid")
    abstract suspend fun findByUidWithContentEntryStatusAsync(contentEntryUid: Long): ContentEntryWithContentEntryStatus?

    @Query("SELECT ContentEntry.*, ContentEntryStatus.* FROM ContentEntry " +
            "LEFT JOIN ContentEntryStatus ON ContentEntryStatus.cesUid = ContentEntry.contentEntryUid " +
            "WHERE ContentEntry.sourceUrl = :sourceUrl")
    @JsName("findBySourceUrlWithContentEntryStatusAsync")
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
    @JsName("getChildrenByParentUidWithCategoryFilter")
    abstract fun getChildrenByParentUidWithCategoryFilter(parentUid: Long, langParam: Long, categoryParam0: Long): DataSource.Factory<Int, ContentEntryWithStatusAndMostRecentContainerUid>

    @Query("SELECT * FROM ContentEntry where contentEntryUid = :parentUid LIMIT 1")
    abstract fun findLiveContentEntry(parentUid: Long): DoorLiveData<ContentEntry?>

    @Query("SELECT contentEntryUid FROM ContentEntry WHERE entryId = :objectId LIMIT 1")
    abstract fun getContentEntryUidFromXapiObjectId(objectId: String): Long
}
