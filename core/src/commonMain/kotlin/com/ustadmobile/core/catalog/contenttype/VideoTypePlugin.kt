package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.controller.VideoContentPresenterCommon.Companion.VIDEO_MIME_MAP
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.VideoPlayerView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage

open class VideoTypePlugin : ContentTypePlugin {

    override val viewName: String
        get() = VideoPlayerView.VIEW_NAME

    override val mimeTypes: Array<String>
        get() = VIDEO_MIME_MAP.keys.toTypedArray()

    override val fileExtensions: Array<String>
        get() = VIDEO_MIME_MAP.values.map { it.removePrefix(".") }.toTypedArray()

    override suspend fun extractMetadata(filePath: String): ContentEntryWithLanguage? {
        TODO("Not yet implemented")
    }

    override suspend fun importToContainer(filePath: String, conversionParams: Map<String, String>, contentEntryUid: Long, mimeType: String, containerBaseDir: String, context: Any, db: UmAppDatabase, repo: UmAppDatabase, progressListener: (Int) -> Unit): Container {
        TODO("Not yet implemented")
    }


}