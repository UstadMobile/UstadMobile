package com.ustadmobile.view

import com.ustadmobile.core.controller.RedirectPresenter
import com.ustadmobile.core.view.RedirectView
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import react.RBuilder

class RedirectComponent (props: UmProps): UstadBaseComponent<UmProps, UmState>(props), RedirectView {

    private var mPresenter: RedirectPresenter? = null

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