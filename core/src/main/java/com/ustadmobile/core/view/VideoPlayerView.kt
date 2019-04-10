package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntry

interface VideoPlayerView : UstadView {

    fun loadUrl(videoPath: String)

    fun setVideoInfo(result: ContentEntry)

    fun setVideoParams(videoPath: String, audioPath: String, srtPath: String)

    companion object {

        val ARG_CONTENT_ENTRY_ID = "entryid"

        val ARG_CONTAINER_UID = "containerUid"

        val VIEW_NAME = "VideoPlayer"
    }
}
