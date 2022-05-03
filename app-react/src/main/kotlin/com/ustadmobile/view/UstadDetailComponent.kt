package com.ustadmobile.view

import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.core.view.UstadDetailView
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import kotlinx.browser.window

abstract class UstadDetailComponent<T: Any>(mProps: UmProps) : UstadBaseComponent<UmProps, UmState>(mProps),
    UstadDetailView<T> {

    abstract val detailPresenter: UstadDetailPresenter<*, *>?

    override var editButtonMode: EditButtonMode = EditButtonMode.GONE
        get() = field
        set(value) {
            window.setTimeout({
                field = value
                fabManager?.visible = (value == EditButtonMode.FAB)
            }, MIN_STATE_CHANGE_DELAY_TIME)
        }

    override fun onCreateView() {
        super.onCreateView()
        fabManager?.icon = "edit"
        fabManager?.text = getString(MessageID.edit)
    }

    override fun onFabClicked() {
        detailPresenter?.handleClickEdit()
    }
}