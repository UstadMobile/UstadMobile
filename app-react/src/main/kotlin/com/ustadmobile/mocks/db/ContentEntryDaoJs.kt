package com.ustadmobile.mocks.db

import androidx.paging.DataSource
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.mocks.db.DatabaseJs.Companion.ALLOW_ACCESS
import com.ustadmobile.mocks.DummyDataPreload.Companion.TAG_ENTRIES
import com.ustadmobile.util.Util.loadDataAsList
import kotlinx.serialization.builtins.ListSerializer

class ContentEntryDaoJs: ContentEntryDao() {

    private val sourcePath = TAG_ENTRIES

    override suspend fun insertListAsync(entityList: List<ContentEntry>) {
        TODO("Not yet implemented")
    }

    override fun downloadedRootItems(
        personUid: Long,
        sortOrder: Int
    ): DataSource.Factory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer> {
        TODO("Not yet implemented")
    }

    override suspend fun findEntryWithLanguageByEntryIdAsync(entryUuid: Long): ContentEntryWithLanguage? {
        TODO("Not yet implemented")
    }

    override suspend fun findEntryWithContainerByEntryId(entryUuid: Long): ContentEntryWithMostRecentContainer? {
        val data = loadDataAsList(sourcePath, ListSerializer(ContentEntryWithMostRecentContainer.serializer()))
        return data.first {
            it.contentEntryUid == entryUuid
        }.apply {
            container = Container()
        }
    }

    override fun findBySourceUrl(sourceUrl: String): ContentEntry? {
        TODO("Not yet implemented")
    }

    override suspend fun findTitleByUidAsync(contentEntryUid: Long): String? {
        val data = loadDataAsList(sourcePath,
            ListSerializer(ContentEntry.serializer()))
        return data.firstOrNull { it.contentEntryUid == contentEntryUid }?.title
    }

    override fun getChildrenByParentUid(parentUid: Long): DataSource.Factory<Int, ContentEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun getChildrenByParentAsync(parentUid: Long): List<ContentEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun getCountNumberOfChildrenByParentUUidAsync(parentUid: Long): Int {
        TODO("Not yet implemented")
    }

    override suspend fun getContentByUuidAsync(parentUid: Long): ContentEntry? {
        val data = loadDataAsList(sourcePath,
            ListSerializer(ContentEntry.serializer()))
        return data.firstOrNull {
            it.contentEntryUid == parentUid
        }
    }

    override suspend fun findAllLanguageRelatedEntriesAsync(entryUuid: Long): List<ContentEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun findListOfCategoriesAsync(parentUid: Long): List<DistinctCategorySchema> {
        TODO("Not yet implemented")
    }

    override suspend fun findUniqueLanguagesInListAsync(parentUid: Long): List<Language> {
        TODO("Not yet implemented")
    }

    override suspend fun findUniqueLanguageWithParentUid(parentUid: Long): List<LangUidAndName> {
        TODO("Not yet implemented")
    }

    override fun update(entity: ContentEntry) {
        TODO("Not yet implemented")
    }

    override suspend fun findByUidAsync(entryUid: Long): ContentEntry? {
        val data = loadDataAsList(sourcePath, ListSerializer(ContentEntry.serializer()))
        return data.firstOrNull { it.contentEntryUid == entryUid }
    }

    override fun findByUid(entryUid: Long): ContentEntry? {
        TODO("Not yet implemented")
    }

    override fun findByTitle(title: String): DoorLiveData<ContentEntry?> {
        TODO("Not yet implemented")
    }

    override suspend fun findBySourceUrlWithContentEntryStatusAsync(sourceUrl: String): ContentEntry? {
        TODO("Not yet implemented")
    }

    override fun getChildrenByParentUidWithCategoryFilterOrderByName(
        parentUid: Long,
        langParam: Long,
        categoryParam0: Long,
        personUid: Long,
        showHidden: Boolean,
        onlyFolder: Boolean,
        sortOrder: Int
    ): DataSource.Factory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer> {
        return DataSourceFactoryJs<Int,ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer, Any>("entryId",parentUid,sourcePath,
            ListSerializer(ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer.serializer()))
    }

    override fun getClazzContent(
        clazzUid: Long,
        personUid: Long,
        sortOrder: Int
    ): DataSource.Factory<Int, ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer> {
        return DataSourceFactoryJs<Int,ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer, Any>("entryId",
            UstadView.MASTER_SERVER_ROOT_ENTRY_UID,sourcePath,
            ListSerializer(ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer.serializer()))
    }

    override suspend fun updateAsync(entity: ContentEntry): Int {
        TODO("Not yet implemented")
    }

    override fun getChildrenByAll(parentUid: Long): List<ContentEntry> {
        TODO("Not yet implemented")
    }

    override fun findLiveContentEntry(parentUid: Long): DoorLiveData<ContentEntry?> {
        TODO("Not yet implemented")
    }

    override fun getContentEntryUidFromXapiObjectId(objectId: String): Long {
        TODO("Not yet implemented")
    }

    override fun findSimilarIdEntryForKhan(sourceUrl: String): List<ContentEntry> {
        TODO("Not yet implemented")
    }

    override suspend fun getRecursiveDownloadTotals(contentEntryUid: Long): DownloadJobSizeInfo? {
        TODO("Not yet implemented")
    }

    override fun getAllEntriesRecursively(contentEntryUid: Long): DataSource.Factory<Int, ContentEntryWithParentChildJoinAndMostRecentContainer> {
        TODO("Not yet implemented")
    }

    override fun getAllEntriesRecursivelyAsList(contentEntryUid: Long): List<ContentEntryWithParentChildJoinAndMostRecentContainer> {
        TODO("Not yet implemented")
    }

    override fun updateContentEntryInActive(contentEntryUid: Long, ceInactive: Boolean) {
        TODO("Not yet implemented")
    }

    override fun updateContentEntryContentFlag(contentFlag: Int, contentEntryUid: Long) {
        TODO("Not yet implemented")
    }

    override fun replaceList(entries: List<ContentEntry>) {
        TODO("Not yet implemented")
    }

    override suspend fun getContentEntryFromUids(contentEntryUids: List<Long>): List<UidAndLabel> {
        TODO("Not yet implemented")
    }

    override fun insertWithReplace(entry: ContentEntry) {
        TODO("Not yet implemented")
    }

    override fun findAllLive(): DoorLiveData<List<ContentEntryWithLanguage>> {
        TODO("Not yet implemented")
    }

    override suspend fun personHasPermissionWithContentEntry(
        accountPersonUid: Long,
        contentEntryUid: Long,
        permission: Long
    ): Boolean {
        return ALLOW_ACCESS
    }

    override suspend fun toggleVisibilityContentEntryItems(
        toggleVisibility: Boolean,
        selectedItem: List<Long>
    ) {
        TODO("Not yet implemented")
    }

    override fun insert(entity: ContentEntry): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertAsync(entity: ContentEntry): Long {
        TODO("Not yet implemented")
    }

    override fun insertList(entityList: List<ContentEntry>) {
        TODO("Not yet implemented")
    }

    override fun updateList(entityList: List<ContentEntry>) {
        TODO("Not yet implemented")
    }

}