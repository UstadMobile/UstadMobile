package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.contentjob.ContentJobProcessContext
import com.ustadmobile.core.contentjob.DummyContentJobItemTransactionRunner
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.runBlocking
import java.io.File
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI

class ContainerDownloadTestCommon {

    companion object {

        fun makeContentJobProcessContext(
            temporaryFolder: TemporaryFolder,
            db: UmAppDatabase,
            di: DI
        ) : ContentJobProcessContext {
            return ContentJobProcessContext(temporaryFolder.newFolder().toDoorUri(),
                temporaryFolder.newFolder().toDoorUri(), params = mutableMapOf(),
                DummyContentJobItemTransactionRunner(db), di)
        }

        fun makeDownloadJobAndJobItem(
            contentEntry: ContentEntry?,
            container: Container?,
            downloadDestDir: File,
            db: UmAppDatabase,
            sourceUriVal: String? = null,
        ) : ContentJobItemAndContentJob {
            return runBlocking {
                ContentJobItemAndContentJob().apply {
                    contentJob = ContentJob().apply {
                        this.toUri = downloadDestDir.toDoorUri().toString()
                        this.cjIsMeteredAllowed = true
                        this.cjUid = db.contentJobDao.insertAsync(this)
                    }
                    contentJobItem = ContentJobItem().apply {
                        this.cjiContentEntryUid = contentEntry?.contentEntryUid ?: 0L
                        if(container != null) {
                            this.cjiContainerUid = container.containerUid
                            this.cjiItemTotal = container.fileSize
                        }

                        this.cjiJobUid = contentJob!!.cjUid
                        this.sourceUri = sourceUriVal
                        this.cjiUid = db.contentJobItemDao.insertJobItem(this)
                    }
                }
            }
        }
    }

}