package com.ustadmobile.core.contentjob

import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.DownloadJobItem
import org.kodein.di.DIAware

interface ContentPlugin : DIAware {

    val jobType: Int

    val supportedMimeTypes: List<String>

    val supportedFileExtensions: List<String>

    suspend fun canProcess(doorUri: DoorUri, process: ProcessContext): Boolean

    suspend fun extractMetadata(uri: DoorUri, process: ProcessContext): ContentEntryWithLanguage?

    suspend fun processJob(jobItem: ContentJobItem, process: ProcessContext): ProcessResult

}