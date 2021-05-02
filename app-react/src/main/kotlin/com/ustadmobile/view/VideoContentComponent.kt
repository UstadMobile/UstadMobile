package com.ustadmobile.view

import com.ustadmobile.core.controller.VideoContentPresenter
import com.ustadmobile.core.controller.VideoContentPresenterCommon
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTAINER_UID
import com.ustadmobile.core.view.VideoContentView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.util.CssStyleManager.responsiveMedia
import com.ustadmobile.util.RouteManager.getArgs
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import react.RBuilder
import react.RProps
import react.RState
import react.setState
import styled.css
import styled.styledVideo

class VideoContentComponent(mProps:RProps):UstadBaseComponent<RProps, RState>(mProps), VideoContentView{

    override var entry: ContentEntry? = null
        get() = field
        set(value) {
            field = value
            title = value?.title
            mPresenter?.onResume()
        }
    override var videoParams: VideoContentPresenterCommon.VideoParams? = null
        get() = field
        set(value) {
            loading = value == null
            setState{field = value}
        }

    private var mPresenter: VideoContentPresenter? = null

    private var db: UmAppDatabase? = null

    private var containerUid: Long = 0

    override fun componentDidMount() {
        super.componentDidMount()
        db = di.on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_DB)
        containerUid = getArgs()[ARG_CONTAINER_UID]?.toLong()?:0L
        mPresenter = VideoContentPresenter(this, getArgs(), this, di)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        styledVideo {
            css(responsiveMedia)
            attrs{
                src = videoParams?.videoPath?:""
                autoPlay = false
                autoBuffer = true
                controls = true
            }
        }
    }
}