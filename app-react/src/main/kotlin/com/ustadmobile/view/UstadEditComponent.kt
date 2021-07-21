package com.ustadmobile.view

import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import react.RProps
import react.RState

abstract class UstadEditComponent<T: Any>(mProps: RProps): UstadBaseComponent<RProps, RState>(mProps), UstadEditView<T> {

    abstract protected val mEditPresenter : UstadEditPresenter<*, T>?

    override fun onCreate() {
        super.onCreate()
        fabManager?.icon = "check"
        fabManager?.text = getString(MessageID.save)
        fabManager?.visible = true
    }

    override fun onFabClicked() {
        super.onFabClicked()
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