package com.ustadmobile.view

import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.controller.UstadSingleEntityPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import kotlinx.browser.window

abstract class UstadEditComponent<T: Any>(mProps: UmProps): UstadBaseComponent<UmProps, UmState>(mProps), UstadEditView<T> {

    abstract protected val mEditPresenter : UstadEditPresenter<*, T>?

    override fun onCreateView() {
        super.onCreateView()
        val fabLabelText = if(mEditPresenter?.persistenceMode == UstadSingleEntityPresenter.PersistenceMode.DB) {
            getString(MessageID.save)
        }else {
            getString(MessageID.done)
        }
        fabManager?.icon = "check"
        fabManager?.text = fabLabelText
        updateUiWithStateChangeDelay {
            fabManager?.visible = true
        }
    }

    override fun onFabClicked() {
        val entityVal = entity ?: return
        mEditPresenter?.handleClickSave(entityVal)
    }

    protected fun setEditTitle(newTitleId: Int, editStringId: Int) {
        window.setTimeout({
            val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
            val entityJsonStr = arguments[ARG_ENTITY_JSON]
            ustadComponentTitle = if(entityUid != 0L || entityJsonStr != null){
                getString(editStringId)
            }else {
                getString(newTitleId)
            }
        }, MAX_STATE_CHANGE_DELAY_TIME)
    }

    override fun onDestroyView() {
        mEditPresenter?.saveStateToNavController()
        super.onDestroyView()
    }
}