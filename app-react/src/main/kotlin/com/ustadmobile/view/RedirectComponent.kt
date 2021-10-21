package com.ustadmobile.view

import com.ustadmobile.core.controller.RedirectPresenter
import com.ustadmobile.core.view.RedirectView
import com.ustadmobile.core.view.UstadView.Companion.ARG_WEB_PLATFORM
import react.RBuilder
import react.RProps
import react.RState

class RedirectComponent (props: RProps): UstadBaseComponent<RProps, RState>(props), RedirectView {

    private var mPresenter: RedirectPresenter? = null

    override val viewName: String
        get() = RedirectView.VIEW_NAME

    override fun onCreateView() {
        super.onCreateView()
        val args = arguments.toMutableMap()
        args[ARG_WEB_PLATFORM] = true.toString()
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