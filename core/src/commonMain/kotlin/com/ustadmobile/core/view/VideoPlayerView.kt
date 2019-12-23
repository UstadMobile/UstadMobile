package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.io.InputStream
import kotlin.js.JsName

interface VideoPlayerView : UstadView {

    @JsName("setVideoInfo")
    fun setVideoInfo(result: ContentEntry)

    @JsName("setVideoParams")
    fun setVideoParams(videoPath: String?, audioPath: InputStream?, srtLangList: MutableList<String>, srtMap: MutableMap<String, String>)

    @JsName("setVideoParamsJs")
    fun setVideoParams(videoPath: String?, audioPath: String?, srtLangList: MutableList<String>, srtMap: MutableMap<String, String>)

    @JsName("showErrorWithAction")
    fun showErrorWithAction(message: String, actionMessageId: Int)


    companion object {

        const val ARG_CONTENT_ENTRY_ID = "entryid"

        const val ARG_CONTAINER_UID = "containerUid"

        const val VIEW_NAME = "VideoPlayer"
    }
}
