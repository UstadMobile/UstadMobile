package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.controller.VideoContentPresenterCommon.Companion.VIDEO_MIME_MAP
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.VideoContentView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage

open class VideoTypePlugin : ContentTypePlugin {

    override val viewName: String
        get() = VideoContentView.VIEW_NAME

    override val mimeTypes: Array<String>
        get() = VIDEO_MIME_MAP.keys.toTypedArray()

    override val fileExtensions: Array<String>
        get() = VIDEO_MIME_MAP.values.map { it.removePrefix(".") }.toTypedArray()

    override suspend fun extractMetadata(uri: String, context: Any): ContentEntryWithLanguage? {
        TODO("Not yet implemented")
    }

    override suspend fun importToContainer(uri: String, conversionParams: Map<String, String>, contentEntryUid: Long, mimeType: String, containerBaseDir: String, context: Any, db: UmAppDatabase, repo: UmAppDatabase, progressListener: (Int) -> Unit): Container {
        TODO("Not yet implemented")
    }


    companion object {

        const val VIDEO_BIT_RATE = 250000

        const val VIDEO_FRAME_INTERVAL = 5

        const val VIDEO_FRAME_RATE = 30

        const val AUDIO_SAMPLE_RATE = 48000

        const val AUDIO_CHANNEL_COUNT = 2

        const val AUDIO_BIT_RATE = 64000

        /**
         * Validate a ratio string that should be in the form of "x:y" where x and y are positive,
         * non-zero integers. ffprobe might return N/A or something other than a valid aspect ratio,
         * so this input needs validated.
         *
         * @return the ratioStr trimmed if it is valid, null otherwise
         */
        fun validateRatio(ratioStr: String): String? {
            val parts = ratioStr.trim().split(':')
            if(parts.size != 2)
                return null //not valid

            val partInts = parts.map { it.toIntOrNull() }
            return if(partInts.all { it != null && it > 0 }) {
                ratioStr.trim()
            }else {
                null
            }
        }

    }


}