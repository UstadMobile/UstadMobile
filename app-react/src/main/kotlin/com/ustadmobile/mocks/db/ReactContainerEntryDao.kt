package com.ustadmobile.mocks.db

import com.ustadmobile.core.db.dao.ContainerEntryDao
import com.ustadmobile.lib.db.entities.ContainerEntry
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5

class ReactContainerEntryDao: ContainerEntryDao() {
    override suspend fun insertListAsync(containerEntryList: List<ContainerEntry>) {
        TODO("Not yet implemented")
    }

    override fun findByContainer(containerUid: Long): List<ContainerEntryWithContainerEntryFile> {
        TODO("Not yet implemented")
    }

    override fun findByPathInContainer(
        containerUid: Long,
        pathInContainer: String
    ): ContainerEntryWithContainerEntryFile? {
        TODO("Not yet implemented")
    }

    override fun findByContainerWithMd5(containerUid: Long): List<ContainerEntryWithMd5> {
        TODO("Not yet implemented")
    }

    override suspend fun findByContainerAsync(containerUid: Long): List<ContainerEntryWithContainerEntryFile> {
        TODO("Not yet implemented")
    }

    override fun deleteByContainerUid(containerUid: Long) {
        TODO("Not yet implemented")
    }

    override fun deleteByContainerEntryUid(containerEntryUid: Long) {
        TODO("Not yet implemented")
    }

    override fun deleteList(entries: List<ContainerEntry>) {
        TODO("Not yet implemented")
    }

    override fun deleteByContentEntryUid(entryUid: Long) {
        TODO("Not yet implemented")
    }

    override fun insert(entity: ContainerEntry): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertAsync(entity: ContainerEntry): Long {
        TODO("Not yet implemented")
    }

    override fun insertList(entityList: List<ContainerEntry>) {
        TODO("Not yet implemented")
    }

    override fun updateList(entityList: List<ContainerEntry>) {
        TODO("Not yet implemented")
    }

    override fun update(entity: ContainerEntry) {
        TODO("Not yet implemented")
    }
}