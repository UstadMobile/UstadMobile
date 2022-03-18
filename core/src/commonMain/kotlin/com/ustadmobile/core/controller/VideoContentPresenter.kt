package com.ustadmobile.core.controller

import com.ustadmobile.core.view.VideoContentView
import org.kodein.di.DI

expect class VideoContentPresenter(context: Any, arguments: Map<String, String>, view: VideoContentView,
                                   di: DI)
    : VideoContentPresenterCommon {

    override fun handleOnResume()
}