package com.ustadmobile.core.container

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import kotlinx.io.InputStream

actual class ContainerManager actual constructor(container: Container,
                                                 db : UmAppDatabase,
                                                 dbRepo: UmAppDatabase,
                                                 newFilePath: String?,
                                                 pathToEntryMap: MutableMap<String, ContainerEntryWithContainerEntryFile>)

    : ContainerManagerCommon(container, db, dbRepo, newFilePath, pathToEntryMap) {

    actual override suspend fun addEntries(addOptions: AddEntryOptions?, vararg entries: EntrySource) {
        TODO("not implemented on JS")
    }

    actual fun getInputStream(containerEntry: ContainerEntryWithContainerEntryFile): InputStream {
        TODO("not implemented on JS") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun getEntry(pathInContainer: String): ContainerEntryWithContainerEntryFile? {
        TODO("not implemented on JS") //To change body of created functions use File | Settings | File Templates.
    }

    actual suspend fun linkExistingItems(itemsToDownload: List<ContainerEntryWithMd5>): List<ContainerEntryWithMd5> {
        TODO("not implemented on JS") //To change body of created functions use File | Settings | File Templates.
    }

     actual override fun exportContainer(zipFile: String,progressListener: ExportProgressListener?){
        TODO("not implemented on JS")
    }

    actual override fun cancelExporting() {
        TODO("not implemented on JS")
    }

    actual override suspend fun addEntries(addOptions: AddEntryOptions?, newPathsToMd5Map: Map<String, ByteArray>, provider: suspend () -> EntrySource?) {
        TODO("not implemented on JS")
    }


}