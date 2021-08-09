package com.ustadmobile.core.contentjob.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.ContentPlugin
import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.getLocalUri
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContentEntryParentChildJoin
import com.ustadmobile.lib.db.entities.ContentJobItem
import org.kodein.di.instance
import org.kodein.di.on

suspend fun ContentPlugin.processMetadata(contentJob: ContentJobItem, process: ProcessContext, context: Any, endpoint: Endpoint): Long{
    if(contentJob.cjiContentEntryUid == 0L){
        val uri = contentJob.fromUri ?: throw IllegalStateException("missing uri")
        val doorUri = DoorUri.parse(uri)
        val localUri = process.getLocalUri(doorUri, context, di)
        val metadata = extractMetadata(localUri, process) ?: throw IllegalArgumentException("missing metadata")
        val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)
        contentJob.cjiContentEntryUid = repo.contentEntryDao.insert(metadata.entry)

        if(contentJob.cjiParentContentEntryUid == 0L){
            return contentJob.cjiContentEntryUid
        }

        ContentEntryParentChildJoin().apply {
            cepcjParentContentEntryUid = contentJob.cjiParentContentEntryUid
            cepcjChildContentEntryUid = contentJob.cjiContentEntryUid
            cepcjUid = repo.contentEntryParentChildJoinDao.insert(this)
        }

        return contentJob.cjiContentEntryUid
    }
    return contentJob.cjiContentEntryUid
}