package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.addEntriesFromZipToContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.sharedse.contentformats.ContainerImporter
import com.ustadmobile.port.sharedse.contentformats.ContentTypeFilePlugin
import java.io.File
import java.net.URI

open class DefaultContainerImporter(var prefixContainer: String = "", var isZipped: Boolean) : ContainerImporter {

    override suspend fun importContentEntryFromFile(contentEntryUid: Long, mimeType: String?, containerBaseDir: String,
                                                    uri: String, db: UmAppDatabase, dbRepo: UmAppDatabase, importMode: Int, context: Any): Container {

        val container = Container().apply {
            containerContentEntryUid = contentEntryUid
        }

        val file = File(uri)
        container.cntLastModified = System.currentTimeMillis()
        container.fileSize = file.length()
        container.mimeType = mimeType
        container.containerUid = dbRepo.containerDao.insert(container)

        val containerManager = ContainerManager(container, db, dbRepo, containerBaseDir)

        if(isZipped){
            addEntriesFromZipToContainer(file.absolutePath, containerManager, prefixContainer)
        }else{
            containerManager.addEntries(ContainerManager.FileEntrySource(file, file.name))
        }

        return container
    }

}