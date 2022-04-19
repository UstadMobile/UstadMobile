package com.ustadmobile.view

import com.ustadmobile.core.controller.SiteTermsDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.SiteTermsDetailView
import com.ustadmobile.lib.db.entities.SiteTerms
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.util.StyleManager.centerContainer
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.ext.renderRawHtmlOnIframe
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import react.RBuilder
import react.setState
import styled.css

class SiteTermsDetailComponent(props: UmProps): UstadDetailComponent<SiteTerms>(props),
    SiteTermsDetailView {

    private var mPresenter: SiteTermsDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    override var acceptButtonVisible: Boolean = false
        get() = field
        set(value) {
            field = value
            updateUiWithStateChangeDelay(MAX_STATE_CHANGE_DELAY_TIME * 2) {
                fabManager?.visible = value
            }
        }

    override var entity: SiteTerms? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override fun onCreateView() {
        super.onCreateView()
        ustadComponentTitle = getString(MessageID.terms_and_policies)
        mPresenter = SiteTermsDetailPresenter(this, arguments, this, this, di)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
        fabManager?.onClickListener = {
            mPresenter?.handleClickAccept()
        }
        fabManager?.text = getString(MessageID.accept)
        fabManager?.icon = "done"
    }

    override fun RBuilder.render() {
        umGridContainer {
            css(centerContainer)

            umItem(GridSize.cells9, GridSize.cells7) {
                renderRawHtmlOnIframe(entity?.termsHtml)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
    }
}