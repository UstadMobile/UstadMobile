package com.ustadmobile.view

import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.core.view.UstadDetailView
import com.ustadmobile.model.statemanager.FabState
import com.ustadmobile.util.StateManager
import org.w3c.dom.events.Event
import react.RProps
import react.RState

abstract class UstadDetailComponent<T: Any>(mProps: RProps) : UstadBaseComponent<RProps,RState>(mProps),
    UstadDetailView<T> {

    abstract val detailPresenter: UstadDetailPresenter<*, *>?

    override fun componentDidMount() {}

    override var editButtonMode: EditButtonMode = EditButtonMode.GONE
        get() = field
        set(value) {
            field = value
            StateManager.dispatch(FabState(visible = value == EditButtonMode.FAB,
                label = systemImpl.getString(MessageID.edit, this),
                    icon = "edit", onClick = ::onFabClick))
        }


    private fun onFabClick(event: Event){
        detailPresenter?.handleClickEdit()
    }


    override fun showSnackBar(message: String, action: () -> Unit, actionMessageId: Int) {
        //handle showing snackbar
    }

}