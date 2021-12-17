package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.contentjob.ContentPlugin
import com.ustadmobile.core.contentjob.ContentJobProcessContext
import com.ustadmobile.core.contentjob.ProcessResult
import com.ustadmobile.core.controller.VideoContentPresenterCommon.Companion.VIDEO_EXT_LIST
import com.ustadmobile.core.controller.VideoContentPresenterCommon.Companion.VIDEO_MIME_MAP
import com.ustadmobile.core.view.VideoContentView

abstract class VideoTypePlugin : ContentPlugin {

    val viewName: String
        get() = VideoContentView.VIEW_NAME

    override val supportedMimeTypes: List<String>
        get() = VIDEO_MIME_MAP.keys.toList()

    override val supportedFileExtensions: List<String>
        get() = VIDEO_EXT_LIST.map { it.removePrefix(".") }

    override val pluginId: Int
        get() = PLUGIN_ID

    companion object {

        const val VIDEO_BIT_RATE = 250000

        const val VIDEO_FRAME_INTERVAL = 5

        const val VIDEO_FRAME_RATE = 30

        const val AUDIO_SAMPLE_RATE = 48000

        const val AUDIO_CHANNEL_COUNT = 2

        const val AUDIO_BIT_RATE = 64000

        const val PLUGIN_ID = 12

    }


}