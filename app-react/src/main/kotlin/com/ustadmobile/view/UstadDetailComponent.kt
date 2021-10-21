package com.ustadmobile.view

import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.core.view.UstadDetailView
import kotlinx.browser.window
import react.RProps
import react.RState

abstract class UstadDetailComponent<T: Any>(mProps: RProps) : UstadBaseComponent<RProps,RState>(mProps),
    UstadDetailView<T> {

    abstract val detailPresenter: UstadDetailPresenter<*, *>?

    override var editButtonMode: EditButtonMode = EditButtonMode.GONE
        get() = field
        set(value) {
            window.setTimeout({
                field = value
                fabManager?.visible = (value == EditButtonMode.FAB)
            }, STATE_CHANGE_DELAY)
        }

    override fun onCreateView() {
        super.onCreateView()
        fabManager?.icon = "edit"
        fabManager?.text = getString(MessageID.edit)
    }

    override fun onFabClicked() {
        super.onFabClicked()
        detailPresenter?.handleClickEdit()
    }
}