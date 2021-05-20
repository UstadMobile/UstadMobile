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
            fabState = fabState.copy(label = systemImpl.getString(MessageID.edit, this),
                icon = "edit", visible = true)
            field = value
        }

    override fun onFabClick(event: Event) {
        super.onFabClick(event)
        detailPresenter?.handleClickEdit()
    }
}