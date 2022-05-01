package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.runBlocking
import java.io.File

class ContainerDownloadTestCommon {

    class ContainerDownloadTestContext(
        val container: Container,
        val contentEntry: ContentEntry
    )

    companion object {

        fun makeDownloadJobAndJobItem(
            contentEntry: ContentEntry,
            container: Container?,
            downloadDestDir: File,
            db: UmAppDatabase,
        ) : ContentJobItemAndContentJob {
            return runBlocking {
                ContentJobItemAndContentJob().apply {
                    contentJob = ContentJob().apply {
                        this.toUri = downloadDestDir.toDoorUri().toString()
                        this.cjIsMeteredAllowed = true
                        this.cjUid = db.contentJobDao.insertAsync(this)
                    }
                    contentJobItem = ContentJobItem().apply {
                        this.cjiContentEntryUid = contentEntry.contentEntryUid
                        if(container != null) {
                            this.cjiContainerUid = container.containerUid
                            this.cjiItemTotal = container.fileSize
                        }

                        this.cjiJobUid = contentJob!!.cjUid
                        this.cjiUid = db.contentJobItemDao.insertJobItem(this)
                    }
                }
            }
        }
    }

}