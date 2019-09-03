package com.ustadmobile.core.container

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.util.getSystemTimeInMillis
import kotlinx.io.InputStream

abstract class ContainerManagerCommon(protected val container: Container,
                                      protected val db : UmAppDatabase,
                                      protected val dbRepo: UmAppDatabase,
                                      protected val newFilePath: String? = null,
                                      protected val pathToEntryMap: MutableMap<String, ContainerEntryWithContainerEntryFile> = mutableMapOf())  {

    data class AddEntryOptions(val moveExistingFiles: Boolean = false,
                               val dontUpdateTotals: Boolean = false)

    var containerUid: Long = 0

    val allEntries: List<ContainerEntryWithContainerEntryFile>
        get() = pathToEntryMap.values.toList()


    init {
        ///load from umDatabase
        val entryList = db.containerEntryDao.findByContainer(container.containerUid)
        containerUid = container.containerUid
        pathToEntryMap.putAll(entryList.map { it.cePath!! to it }.toMap())
    }

    interface EntrySource {

        /**
         * Length of the entry that is going to be added. This is used only for progress purposes
         */
        val length : Long

        /**
         * The path this entry will have inside the container e.g. META-INF/container.xml
         */
        val pathInContainer: String


        /**
         * An inputstream that provides the contents of the entry
         */
        val inputStream: InputStream

        /**
         * The path to this file. If there is no actual file, e.g. this is being added from a download,
         * then this might be null
         */
        val filePath : String?

        /**
         * The MD5 sum of the entry being added (if known)
         */
        val md5Sum: ByteArray

        /**
         * The compression (if this file is already compressed)
         */
        val compression: Int

    }


    /**
     * Make a copy of this container as a new container - e.g. when making a new version of this
     * file, adding files, etc.
     *
     * @return ContainerManager wiht the same contents, linked to the same underlying files, with the
     * last modified timestamp updated.
     */
    fun copyToNewContainer(): ContainerManager {
        val newContainer = Container()
        newContainer.fileSize = container.fileSize
        newContainer.lastModified = getSystemTimeInMillis()
        newContainer.cntNumEntries = pathToEntryMap.size
        newContainer.containerContentEntryUid = container.containerContentEntryUid
        newContainer.mimeType = container.mimeType
        newContainer.mobileOptimized = container.mobileOptimized
        newContainer.remarks = container.remarks
        newContainer.containerUid = dbRepo.containerDao.insert(newContainer)

        val newEntryMap = pathToEntryMap.map { it.key to
                ContainerEntryWithContainerEntryFile(it.value.cePath!!, newContainer, it.value.containerEntryFile!!)
        }.toMap()

        db.containerEntryDao.insertList(newEntryMap.values.map { it })
        return ContainerManager(newContainer, db, dbRepo, newFilePath,
                newEntryMap.toMutableMap())
    }

    abstract suspend fun addEntries(addOptions: AddEntryOptions?, vararg entries: EntrySource)

    suspend fun addEntries(vararg entries: EntrySource) = addEntries(null, *entries)

}