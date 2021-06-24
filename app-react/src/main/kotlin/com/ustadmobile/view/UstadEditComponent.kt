package com.ustadmobile.view

import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.w3c.dom.events.Event
import react.RProps
import react.RState

abstract class UstadEditComponent<T: Any>(mProps: RProps): UstadBaseComponent<RProps, RState>(mProps), UstadEditView<T> {

    abstract protected val mEditPresenter : UstadEditPresenter<*, T>?

    override fun onComponentReady() {
        super.onComponentReady()
        fabState = fabState.copy(visible = true, icon = "check",
            title = getString(MessageID.save))
    }

    override fun onFabClicked(event: Event) {
        super.onFabClicked(event)
        val entityVal = entity ?: return
        mEditPresenter?.handleClickSave(entityVal)
    }

    override fun finishWithResult(result: List<T>) {}

    protected fun setEditTitle(newTitleId: Int, editStringId: Int) {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val entityJsonStr = arguments[ARG_ENTITY_JSON]
        title = if(entityUid != 0L || entityJsonStr != null){
           getString(editStringId)
        }else {
           getString(newTitleId)
        }
    }
}