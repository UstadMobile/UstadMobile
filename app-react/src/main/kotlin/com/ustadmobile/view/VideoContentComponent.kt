package com.ustadmobile.view

import com.ustadmobile.core.controller.VideoContentPresenter
import com.ustadmobile.core.controller.VideoContentPresenterCommon
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTAINER_UID
import com.ustadmobile.core.view.VideoContentView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.util.StyleManager.videoComponentResponsiveMedia
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import react.RBuilder
import react.setState
import styled.css
import styled.styledVideo

class VideoContentComponent(mProps: UmProps):UstadBaseComponent<UmProps, UmState>(mProps), VideoContentView{

    override var entry: ContentEntry? = null
        get() = field
        set(value) {
            field = value
            ustadComponentTitle = value?.title
            mPresenter?.onResume()
        }

    override val viewNames: List<String>
        get() = listOf(VideoContentView.VIEW_NAME)

    override var videoParams: VideoContentPresenterCommon.VideoParams? = null
        get() = field
        set(value) {
            loading = value == null
            setState{
                field = value
            }
        }

    private var mPresenter: VideoContentPresenter? = null

    private var db: UmAppDatabase? = null

    private var containerUid: Long = 0

    override fun onCreateView() {
        super.onCreateView()
        db = di.on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_DB)
        containerUid = arguments[ARG_CONTAINER_UID]?.toLong() ?: 0L
        mPresenter = VideoContentPresenter(this, arguments, this, di)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        styledVideo {
            css(videoComponentResponsiveMedia)
            attrs.src = videoParams?.videoPath ?: ""
            attrs.autoPlay = false
            attrs.autoBuffer = true
            attrs.controls = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entry = null
        videoParams = null
        db = null
    }
}