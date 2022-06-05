package com.ustadmobile.view

import com.ustadmobile.core.controller.HtmlTextViewDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.view.HtmlTextViewDetailView
import com.ustadmobile.util.StyleManager.contentContainer
import com.ustadmobile.util.StyleManager.defaultDoubleMarginTop
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.ext.umGridContainer
import react.RBuilder
import react.setState
import styled.css

class HtmlTextViewComponent(mProps: UmProps): UstadDetailComponent<String>(mProps),
    HtmlTextViewDetailView {

    private var mPresenter: HtmlTextViewDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override fun onCreateView() {
        super.onCreateView()
        mPresenter = HtmlTextViewDetailPresenter(this, arguments, this,
            this,di)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {
        umGridContainer {
            css{
                +contentContainer
                +defaultDoubleMarginTop
            }
            umMuiHtmlEditor(entity, true, toolbar = false)
        }
    }


    override var title: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
            updateUiWithStateChangeDelay {
                ustadComponentTitle = value
            }
        }


    override var entity: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
    }

}