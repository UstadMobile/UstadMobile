package com.ustadmobile.view

import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.core.view.UstadDetailView
import org.w3c.dom.events.Event
import react.RProps
import react.RState

abstract class UstadDetailComponent<T: Any>(mProps: RProps) : UstadBaseComponent<RProps,RState>(mProps),
    UstadDetailView<T> {

    abstract val detailPresenter: UstadDetailPresenter<*, *>?

    override var editButtonMode: EditButtonMode = EditButtonMode.GONE
        get() = field
        set(value) {
            fabState = fabState.copy(title = getString(MessageID.edit),
                icon = "edit", visible = true)
            field = value
        }

    override fun onFabClicked(event: Event) {
        super.onFabClicked(event)
        detailPresenter?.handleClickEdit()
    }
}