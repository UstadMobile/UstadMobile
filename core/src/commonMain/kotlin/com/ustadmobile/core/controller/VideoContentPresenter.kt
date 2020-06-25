package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.VideoPlayerView

expect class VideoContentPresenter(context: Any, arguments: Map<String, String>?, view: VideoPlayerView,
                                   db: UmAppDatabase, repo: UmAppDatabase)
    : VideoContentPresenterCommon {

    override fun handleOnResume()
}