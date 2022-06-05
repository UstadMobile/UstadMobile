package com.ustadmobile.view

import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.controller.UstadSingleEntityPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import io.github.aakira.napier.Napier
import kotlinx.browser.window

abstract class UstadEditComponent<T: Any>(mProps: UmProps): UstadBaseComponent<UmProps, UmState>(mProps), UstadEditView<T> {

    abstract protected val mEditPresenter : UstadEditPresenter<*, T>?

    override fun onCreateView() {
        Napier.d("UstadEditComponent: navController viewName = ${navController.currentBackStackEntry?.viewName} ")
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
        /**
         * If the user is on an EditPresenter that saves to the database, and moving away (back or
         * forwards), make sure that we have saved the "draft" to the state handle.
         *
         * When the user is navigating between edit screens using navigateForResult, this is already
         * taken care of. On the browser, the user can go forward themselves.
         */
        val presenterVal = mEditPresenter
        if(presenterVal != null
                && presenterVal.persistenceMode == UstadSingleEntityPresenter.PersistenceMode.DB
                && (systemTimeInMillis() -  presenterVal.lastStateSaveTime) > LAST_SAVE_CHECK_WINDOW) {
            presenterVal.saveStateToNavController()
        }

        super.onDestroyView()
    }

    companion object {

        const val LAST_SAVE_CHECK_WINDOW = 100

    }
}