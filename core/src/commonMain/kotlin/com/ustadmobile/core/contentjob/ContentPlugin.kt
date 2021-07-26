package com.ustadmobile.core.contentjob

import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.DownloadJobItem

interface ContentPlugin {

    val jobType: Int

    val supportedMimeTypes: List<String>

    val supportedFileExtensions: List<String>

    suspend fun canProcess(doorUri: DoorUri): Boolean

    suspend fun extractMetadata(uri: DoorUri): ContentEntryWithLanguage?

    suspend fun processJob(jobItem: ContentJobItem): ProcessResult

}