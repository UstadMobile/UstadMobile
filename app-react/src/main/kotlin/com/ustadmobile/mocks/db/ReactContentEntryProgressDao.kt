package com.ustadmobile.mocks.db

import com.ustadmobile.core.db.dao.ContentEntryProgressDao
import com.ustadmobile.lib.db.entities.ContentEntryProgress

class ReactContentEntryProgressDao: ContentEntryProgressDao() {
    override suspend fun updateAsync(contentEntryProgress: ContentEntryProgress): Int {
        TODO("Not yet implemented")
    }

    override fun getProgressByContentAndPerson(
        contentEntryUid: Long,
        personUid: Long
    ): ContentEntryProgress? {
        TODO("Not yet implemented")
    }

    override suspend fun getProgressByContentAndPersonAsync(
        contentEntryUid: Long,
        personUid: Long
    ): ContentEntryProgress? {
        return ContentEntryProgress()
    }

    override fun updateProgressByContentEntryAndPerson(
        contentEntryUid: Long,
        personUid: Long,
        progress: Int,
        status: Int
    ): Int {
        TODO("Not yet implemented")
    }

    override fun insert(entity: ContentEntryProgress): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertAsync(entity: ContentEntryProgress): Long {
        TODO("Not yet implemented")
    }

    override fun insertList(entityList: List<ContentEntryProgress>) {
        TODO("Not yet implemented")
    }

    override fun updateList(entityList: List<ContentEntryProgress>) {
        TODO("Not yet implemented")
    }

    override fun update(entity: ContentEntryProgress) {
        TODO("Not yet implemented")
    }
}