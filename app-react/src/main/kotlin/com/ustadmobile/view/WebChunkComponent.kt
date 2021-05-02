package com.ustadmobile.view

import com.ustadmobile.core.controller.WebChunkPresenter
import com.ustadmobile.core.view.WebChunkView
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.util.CssStyleManager
import com.ustadmobile.util.RouteManager.getArgs
import react.RBuilder
import react.RProps
import react.RState
import styled.css
import styled.styledIframe

class WebChunkComponent(mProps: RProps): UstadBaseComponent<RProps, RState>(mProps), WebChunkView  {

    private var mPresenter: WebChunkPresenter? = null

    override var entry: ContentEntry? = null
        get() = field
        set(value) {
            field = value
            title = value?.title
        }
    override var url: String = ""
        get() = field
        set(value) {
            loading = value.isEmpty()
            field = value
        }

    override fun componentDidMount() {
        super.componentDidMount()
        mPresenter = WebChunkPresenter(this, getArgs(), this, di)
        mPresenter?.onCreate(mapOf())
    }


    override fun RBuilder.render() {
        if(url.isNotEmpty()){
            styledIframe {
                css(CssStyleManager.responsiveIframe)
                attrs{
                    src = url
                }
            }
        }
    }

    override fun showNoAppFoundError(message: String, actionMessageId: Int, mimeType: String) {
        showSnackBar(message, {}, actionMessageId)
    }

}