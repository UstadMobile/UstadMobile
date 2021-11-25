package com.ustadmobile.view

import com.ustadmobile.core.controller.RedirectPresenter
import com.ustadmobile.core.view.RedirectView
import react.RBuilder
import com.ustadmobile.util.*

class RedirectComponent (props: UmProps): UstadBaseComponent<UmProps, UmState>(props), RedirectView {

    private var mPresenter: RedirectPresenter? = null

    override val viewName: String
        get() = RedirectView.VIEW_NAME

    override fun onCreateView() {
        super.onCreateView()
        val args = arguments.toMutableMap()
        mPresenter = RedirectPresenter(this, args, this, di)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.render() {}

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }
}