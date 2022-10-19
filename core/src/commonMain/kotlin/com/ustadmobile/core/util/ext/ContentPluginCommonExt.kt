package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.ContentJobProcessContext
import com.ustadmobile.core.contentjob.ContentJobProgressListener
import com.ustadmobile.core.contentjob.ContentPlugin
import com.ustadmobile.core.contentjob.ProcessResult
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ContainerBuilder
import com.ustadmobile.core.io.ext.isRemote
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class ProcessContentImportContext{

    var estimatedSize: Long = -1

    internal var containerBlock: (ContainerBuilder.() -> Unit)? = null

    fun container(
        block: ContainerBuilder.() -> Unit
    ){
        containerBlock = block
    }

}

suspend fun ContentPlugin.processContentImport(
    processContext: ContentJobProcessContext,
    jobItem: ContentJobItemAndContentJob,
    progressListener: ContentJobProgressListener,
    endpoint: Endpoint,
    context: Any,
    block: suspend ProcessContentImportContext.() -> Unit
) : ProcessResult {
    val contentJobItem = jobItem.contentJobItem
        ?: throw IllegalArgumentException("missing job item")
    val db: UmAppDatabase = on(endpoint).direct.instance(tag = DoorTag.TAG_DB)
    val repo: UmAppDatabase = on(endpoint).direct.instance(tag = DoorTag.TAG_REPO)

    val localUri = processContext.getLocalOrCachedUri()
    contentJobItem.updateTotalFromLocalUriIfNeeded(localUri, localUri.isRemote(),
        progressListener, context, di)

    if(!contentJobItem.cjiContainerProcessed) {
        val containerFolder = jobItem.contentJob?.toUri
            ?: defaultContainerDir.toURI().toString()
    }

    TODO()
}