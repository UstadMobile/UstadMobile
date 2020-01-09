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

    var exporting: Boolean = false

    lateinit var destinationZipFile: String

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
         * The paths this entry will have inside the container e.g. META-INF/container.xml. This is
         * normally a list of just 1, but it is theoretically possible that a container could have
         * multiple files that have the same contents (e.g. the same md5 sum).
         */
        val pathsInContainer: List<String>


        /**
         * An inputstream that provides the contents of the entry. This will only be read once.
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

        /**
         * Dispose of any underlying resources. If the InputStream is no longer going to be used,
         * it should be closed here. If the InputStream will be closed later (e.g. where multiple
         * entries are being read from single stream such as a concatenatedinputstream or zip) then
         * no action needs to be taken.
         */
        fun dispose()

    }

    interface ExportProgressListener{

        /**
         * Report export progress to all listening parts
         */
        fun onProcessing(progress: Int)

        fun onDone()

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
        newContainer.cntLastModified = getSystemTimeInMillis()
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

    abstract suspend fun addEntries(addOptions: AddEntryOptions?,
                                           newPathsToMd5Map: Map<String, ByteArray>,
                                           provider: suspend () -> EntrySource?)

    abstract fun exportContainer(zipFile: String,progressListener: ExportProgressListener?)

    suspend fun addEntries(vararg entries: EntrySource) = addEntries(null, *entries)

    abstract fun cancelExporting()

}