package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import org.kodein.di.DI

class LottiePlugin : ContentPlugin {

    override val di: DI
        get() = TODO("Not yet implemented")

    override val pluginId: Int
        get() = TODO("Not yet implemented")

    override val supportedMimeTypes: List<String>
        get() = TODO("Not yet implemented")

    override val supportedFileExtensions: List<String>
        get() = TODO("Not yet implemented")

    override suspend fun extractMetadata(
        uri: DoorUri,
        process: ContentJobProcessContext
    ): MetadataResult? {
        //TODO: Get the name from the Lottie file
        TODO("Not yet implemented")
    }

    override suspend fun processJob(
        jobItem: ContentJobItemAndContentJob,
        process: ContentJobProcessContext,
        progress: ContentJobProgressListener
    ): ProcessResult {
        TODO("Not yet implemented")
    }
}