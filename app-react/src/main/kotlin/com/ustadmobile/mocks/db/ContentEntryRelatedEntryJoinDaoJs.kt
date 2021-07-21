package com.ustadmobile.mocks.db

import androidx.paging.DataSource
import com.ustadmobile.core.db.dao.ContentEntryRelatedEntryJoinDao
import com.ustadmobile.lib.db.entities.*

class ContentEntryRelatedEntryJoinDaoJs: ContentEntryRelatedEntryJoinDao() {

    private val relationPath = ""

    override fun publicContentEntryRelatedEntryJoins(): List<ContentEntryRelatedEntryJoin> {
        TODO("Not yet implemented")
    }

    override fun findPrimaryByTranslation(contentEntryUid: Long): ContentEntryRelatedEntryJoin? {
        TODO("Not yet implemented")
    }

    override suspend fun findAllTranslationsForContentEntryAsync(contentEntryUid: Long): List<ContentEntryRelatedEntryJoinWithLangName> {
        TODO("Not yet implemented")
    }

    override fun findAllTranslationsWithContentEntryUid(contentEntryUid: Long): DataSource.Factory<Int, ContentEntryRelatedEntryJoinWithLanguage> {
        val entries = ContentEntryDaoJs.ENTRIES.filter { it.leaf }
            .unsafeCast<List<ContentEntryRelatedEntryJoinWithLanguage>>()
        entries.mapIndexed { index, it ->
                it.language = LanguageDaoJs.ENTRIES[index]
        }
        return DataSourceFactoryJs(entries)
    }

    override fun update(entity: ContentEntryRelatedEntryJoin) {
        TODO("Not yet implemented")
    }

    override fun insert(entity: ContentEntryRelatedEntryJoin): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertAsync(entity: ContentEntryRelatedEntryJoin): Long {
        TODO("Not yet implemented")
    }

    override fun insertList(entityList: List<ContentEntryRelatedEntryJoin>) {
        TODO("Not yet implemented")
    }

    override fun updateList(entityList: List<ContentEntryRelatedEntryJoin>) {
        TODO("Not yet implemented")
    }
}