package com.ustadmobile.view

import com.ustadmobile.core.controller.XapiPackageContentPresenter
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.util.CssStyleManager.responsiveIframe
import com.ustadmobile.util.RouteManager.getArgs
import react.RBuilder
import react.RProps
import react.RState
import react.setState
import styled.css
import styled.styledIframe

class XapiPackageContentComponent(mProps: RProps): UstadBaseComponent<RProps, RState>(mProps),
    XapiPackageContentView {

    override var contentTitle: String = ""
        get() = field
        set(value) {
            field = value
            title = value
        }
    override var url: String = ""
        get() = field
        set(value) {
            console.log(value)
            setState { field = value }
        }

    private lateinit var mPresenter: XapiPackageContentPresenter

    override fun componentDidMount() {
        super.componentDidMount()
        mPresenter = XapiPackageContentPresenter(this,getArgs(),this,di)
        mPresenter.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        styledIframe {
            css(responsiveIframe)
            attrs{
                src = url
            }
        }
    }
}