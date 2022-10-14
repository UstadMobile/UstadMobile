package com.ustadmobile.view

import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.controller.UstadSingleEntityPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.navigation.NavControllerJs
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.UmState
import io.github.aakira.napier.Napier
import kotlinx.browser.window
import kotlinx.css.LinearDimension
import kotlinx.css.width
import mui.material.ButtonColor
import mui.material.styles.TypographyVariant
import react.Props
import react.RBuilder
import react.ReactElement
import react.setState
import styled.css
import styled.styledDiv
import kotlin.js.Date

abstract class UstadEditComponent<T: Any>(mProps: UmProps): UstadBaseComponent<UmProps, UmState>(mProps), UstadEditView<T> {

    protected abstract val mEditPresenter : UstadEditPresenter<*, T>?

    protected var saveOrDiscardDialogVisible: Boolean = false

    private val lastSaveOrDiscardedTime: Long
        get() = savedStateHandle?.get<String>(UstadEditPresenter.KEY_LAST_SAVE_OR_DISCARD_TIME)?.toLong() ?: 0

    override fun onCreateView() {
        if(lastSaveOrDiscardedTime == 0L && (navController as NavControllerJs).wentForwardForSavePrompt) {
            saveOrDiscardDialogVisible = true
            (navController as NavControllerJs).wentForwardForSavePrompt = false
        }

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

        if(lastSaveOrDiscardedTime == 0L && mEditPresenter?.hasUnsavedChanges() == true) {
            (navController as NavControllerJs).wentForwardForSavePrompt = true
            loading = false
            window.history.forward()
        }


        super.onDestroyView()
    }

    override fun showSaveOrDiscardChangesDialog() {

    }

    fun RBuilder.renderSaveOrDiscardChangesDialog() {
        if(saveOrDiscardDialogVisible) {
            fun discard() {
                mEditPresenter?.stampLastSavedOrDiscardTime()
                loading = false
                window.history.back()
            }


            umDialog(true, onClose = { saveOrDiscardDialogVisible = false }) {
                umDialogContent {
                    css {
                        width = LinearDimension("100%")
                    }
                    umTypography(systemImpl.getString(MessageID.your_changes_have_not_been_saved, this),
                        variant = TypographyVariant.body1){
                        css(StyleManager.alignTextToStart)
                    }
                }

                umDialogActions {
                    umButton(
                        systemImpl.getString(MessageID.save, this),
                        color = ButtonColor.secondary,
                        onClick = {
                            entity?.also { mEditPresenter?.handleClickSave(it) }
                        }
                    )
                    umButton(
                        systemImpl.getString(MessageID.discard, this),
                        color = ButtonColor.secondary,
                        onClick = {
                            discard()
                        }
                    )
                }
            }
        }
    }

    companion object {

        const val LAST_SAVE_CHECK_WINDOW = 100

    }
}