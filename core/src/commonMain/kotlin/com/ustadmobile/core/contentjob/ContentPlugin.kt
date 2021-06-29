package com.ustadmobile.core.contentjob

import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.DownloadJobItem

interface ContentPlugin {

    val jobType: Int

    val supportedMimeTypes: List<String>

    val supportedFileExtensions: List<String>

    suspend fun canProcess(doorUri: DoorUri): Boolean

    suspend fun extractMetadata(uri: DoorUri): ContentEntryWithLanguage?

    //TODO: DownloadJobItem will be renamed to ContentJobItem
    //ContentJobItem will need fields for jobType, fromUri, toUri
    suspend fun processJob(jobItem: DownloadJobItem): ProcessResult

}