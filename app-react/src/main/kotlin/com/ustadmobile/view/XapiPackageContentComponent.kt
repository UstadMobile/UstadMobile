package com.ustadmobile.view

import com.ustadmobile.core.controller.XapiPackageContentPresenter
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import react.RBuilder
import react.setState

class XapiPackageContentComponent(mProps: UmProps): UstadBaseComponent<UmProps, UmState>(mProps),
    XapiPackageContentView {

    override var contentTitle: String = ""
        get() = field
        set(value) {
            field = value
            ustadComponentTitle = value
        }

    override var viewNames: List<String>? = null

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
        mPresenter = XapiPackageContentPresenter(this,arguments,this,di)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {
        renderIframe(listOf(url), 1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        viewNames = null
    }
}