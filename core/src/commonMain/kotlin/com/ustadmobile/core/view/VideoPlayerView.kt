package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.io.InputStream

interface VideoPlayerView : UstadView {

    fun setVideoInfo(result: ContentEntry)

    fun setVideoParams(videoPath: String?, audioPath: InputStream?, srtLangList: MutableList<String>, srtMap: MutableMap<String, String>)

    companion object {

        const val ARG_CONTENT_ENTRY_ID = "entryid"

        const val ARG_CONTAINER_UID = "containerUid"

        const val VIEW_NAME = "VideoPlayer"
    }
}
