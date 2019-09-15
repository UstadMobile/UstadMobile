package com.ustadmobile.core.container

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import kotlinx.io.IOException
import kotlinx.io.InputStream

expect class ContainerManager : ContainerManagerCommon {

    constructor(container: Container,
                db : UmAppDatabase,
                dbRepo: UmAppDatabase,
                newFilePath: String? = null,
                pathToEntryMap: MutableMap<String, ContainerEntryWithContainerEntryFile> = mutableMapOf())


    override suspend fun addEntries(addOptions: AddEntryOptions?, vararg entries: EntrySource)

    fun getInputStream(containerEntry: ContainerEntryWithContainerEntryFile): InputStream

    fun getEntry(pathInContainer: String): ContainerEntryWithContainerEntryFile?

    override fun exportContainer(zipFile: String, progressListener: ExportProgressListener?)

    suspend fun linkExistingItems(itemsToDownload: List<ContainerEntryWithMd5>) : List<ContainerEntryWithMd5>

    override fun cancelExporting()

}