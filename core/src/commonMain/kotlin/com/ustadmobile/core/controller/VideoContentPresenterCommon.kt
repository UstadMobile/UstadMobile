package com.ustadmobile.core.controller


abstract class VideoContentPresenterCommon() {

    companion object {

        val VIDEO_EXT_LIST = listOf("mp4", "mkv", "webm", "m4v")

        var VIDEO_MIME_MAP = mapOf("video/mp4" to ".mp4",
            "video/x-matroska" to ".mkv",
            "video/webm" to ".webm",
            "video/x-m4v" to ".m4v")
    }
}
