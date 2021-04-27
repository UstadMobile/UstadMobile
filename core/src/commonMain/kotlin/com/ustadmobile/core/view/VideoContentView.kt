package com.ustadmobile.core.view

import com.ustadmobile.core.controller.VideoContentPresenterCommon
import com.ustadmobile.lib.db.entities.ContentEntry

interface VideoContentView : UstadView {

    var entry: ContentEntry?

    var videoParams: VideoContentPresenterCommon.VideoParams?

    companion object {

        const val VIEW_NAME = "VideoContentView"
    }
}
