package com.ustadmobile.core.view

import com.ustadmobile.core.controller.VideoPlayerPresenter
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.io.InputStream

interface VideoPlayerView : UstadView {

    fun loadUrl(videoPath: String)

    fun setVideoInfo(result: ContentEntry)

    fun setVideoParams(videoParams: VideoPlayerPresenter.VideoParams)

    companion object {

        const val ARG_CONTENT_ENTRY_ID = "entryid"

        const val ARG_CONTAINER_UID = "containerUid"

        const val VIEW_NAME = "VideoPlayer"
    }
}
