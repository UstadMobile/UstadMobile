package com.ustadmobile.core.controller

import com.ustadmobile.core.view.VideoPlayerView
import org.kodein.di.DI

expect class VideoContentPresenter(context: Any, arguments: Map<String, String>, view: VideoPlayerView,
                                   di: DI)
    : VideoContentPresenterCommon {

    override fun handleOnResume()
}