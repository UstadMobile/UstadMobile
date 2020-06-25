package com.ustadmobile.core.view

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.controller.VideoContentPresenterCommon
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.io.InputStream
import kotlin.js.JsName

interface VideoPlayerView : UstadView {

    var entry: ContentEntry?

    var videoParams: VideoContentPresenterCommon.VideoParams?

    var containerManager: ContainerManager?

    companion object {

        const val VIEW_NAME = "VideoPlayer"
    }
}
