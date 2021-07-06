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

    private lateinit var arguments: Map<String,String>

    override fun onCreate(arguments: Map<String, String>) {
        super.onCreate(arguments)
        this.arguments = arguments
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