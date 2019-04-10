package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntry

interface VideoPlayerView : UstadView {

    fun loadUrl(videoPath: String)

    fun setVideoInfo(result: ContentEntry)

    fun setVideoParams(videoPath: String, audioPath: String, srtPath: String)

    companion object {

        const val ARG_CONTENT_ENTRY_ID = "entryid"

        const val ARG_CONTAINER_UID = "containerUid"

        const val VIEW_NAME = "VideoPlayer"
    }
}
