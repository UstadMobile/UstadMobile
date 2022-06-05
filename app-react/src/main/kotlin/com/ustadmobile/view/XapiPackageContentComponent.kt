package com.ustadmobile.view

import com.ustadmobile.core.controller.XapiPackageContentPresenter
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import kotlinx.css.height
import kotlinx.css.vh
import react.RBuilder
import react.dom.attrs
import react.setState
import styled.css
import styled.styledIframe

class XapiPackageContentComponent(mProps: UmProps): UstadBaseComponent<UmProps, UmState>(mProps),
    XapiPackageContentView {

    override var contentTitle: String = ""
        get() = field
        set(value) {
            field = value
            ustadComponentTitle = value
        }

    override var url: String = ""
        get() = field
        set(value) {
            loading = value.isEmpty()
            setState {
                field = value
            }
        }

    private var mPresenter: XapiPackageContentPresenter? = null

    override fun onCreateView() {
        super.onCreateView()
        fabManager?.visible = false
        mPresenter = XapiPackageContentPresenter(this,arguments,this,di)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        if(url.isNotEmpty()) {
            styledIframe {
                css(StyleManager.iframeComponentResponsiveIframe)
                css {
                    height = 80.vh
                }
                attrs {
                    src = url
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }
}