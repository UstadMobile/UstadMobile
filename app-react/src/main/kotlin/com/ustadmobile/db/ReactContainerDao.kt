package com.ustadmobile.db

import com.ustadmobile.core.db.dao.ContainerDao
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerUidAndMimeType
import com.ustadmobile.lib.db.entities.ContainerWithContentEntry

class ReactContainerDao: ContainerDao() {
    override suspend fun insertListAsync(containerList: List<Container>) {
        TODO("Not yet implemented")
    }

    override fun insertListAndReturnIds(containerList: List<Container>): Array<Long> {
        TODO("Not yet implemented")
    }

    override suspend fun getMostRecentDownloadedContainerForContentEntryAsync(contentEntry: Long): Container? {
        TODO("Not yet implemented")
    }

    override fun getMostRecentContainerForContentEntry(contentEntry: Long): Container? {
        TODO("Not yet implemented")
    }

    override fun getMostRecentContainerForContentEntryLive(contentEntry: Long): DoorLiveData<Container?> {
        TODO("Not yet implemented")
    }

    override fun getFileSizeOfMostRecentContainerForContentEntry(contentEntryUid: Long): Long {
        TODO("Not yet implemented")
    }

    override fun findByUid(uid: Long): Container? {
        TODO("Not yet implemented")
    }

    override suspend fun findRecentContainerToBeMonitoredWithEntriesUid(contentEntries: List<Long>): List<Container> {
        TODO("Not yet implemented")
    }

    override suspend fun findFilesByContentEntryUid(contentEntryUid: Long): List<Container> {
        TODO("Not yet implemented")
    }

    override fun findAllPublikContainers(): List<Container> {
        TODO("Not yet implemented")
    }

    override suspend fun findByUidAsync(containerUid: Long): Container? {
        TODO("Not yet implemented")
    }

    override fun updateContainerSizeAndNumEntries(containerUid: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun updateContainerSizeAndNumEntriesAsync(containerUid: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun updateFileSizeForAllContainers() {
        TODO("Not yet implemented")
    }

    override fun findLocalAvailabilityByUid(containerUid: Long): Long {
        TODO("Not yet implemented")
    }

    override fun findAllWithId(containerUid: Long): List<Container> {
        TODO("Not yet implemented")
    }

    override fun findKhanContainers(): List<ContainerWithContentEntry> {
        TODO("Not yet implemented")
    }

    override fun deleteByUid(containerUid: Long) {
        TODO("Not yet implemented")
    }

    override fun updateMimeType(mimeType: String, containerUid: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun getMostRecentContainerForContentEntryAsync(contentEntry: Long): Container? {
        TODO("Not yet implemented")
    }

    override suspend fun getMostRecentContaineUidAndMimeType(contentEntry: Long): ContainerUidAndMimeType? {
        return when(contentEntry){
            41250L -> ContainerUidAndMimeType().apply {
                mimeType = "application/epub+zip"
            }
            13112L -> ContainerUidAndMimeType().apply {
                mimeType = "application/h5p-tincan+zip"
            }
            62506L -> ContainerUidAndMimeType().apply {
                mimeType = "application/webchunk+zip"
            }
            59108L -> ContainerUidAndMimeType().apply {
                mimeType = "application/har+zip"
            }
            else -> ContainerUidAndMimeType().apply{
                mimeType = "application/khan-video+zip"
            }
        }
    }

    override fun replaceList(entries: List<Container>) {
        TODO("Not yet implemented")
    }

    override fun insertWithReplace(container: Container) {
        TODO("Not yet implemented")
    }

    override fun insert(entity: Container): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertAsync(entity: Container): Long {
        TODO("Not yet implemented")
    }

    override fun insertList(entityList: List<Container>) {
        TODO("Not yet implemented")
    }

    override fun updateList(entityList: List<Container>) {
        TODO("Not yet implemented")
    }

    override fun update(entity: Container) {
        TODO("Not yet implemented")
    }

}