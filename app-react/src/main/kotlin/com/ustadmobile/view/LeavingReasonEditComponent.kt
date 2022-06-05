package com.ustadmobile.view

import com.ustadmobile.util.FieldLabel
import com.ustadmobile.core.controller.LeavingReasonEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.LeavingReasonEditView
import com.ustadmobile.lib.db.entities.LeavingReason
import com.ustadmobile.mui.components.FormControlVariant
import com.ustadmobile.mui.components.GridSize
import com.ustadmobile.mui.components.umTextField
import com.ustadmobile.util.StyleManager.fieldsOnlyFormScreen
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import react.RBuilder
import react.setState
import styled.css

class LeavingReasonEditComponent (mProps: UmProps): UstadEditComponent<LeavingReason>(mProps),
    LeavingReasonEditView {

    private var mPresenter: LeavingReasonEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, LeavingReason>?
        get() = mPresenter

    private var reasonTitle = FieldLabel(text = getString(MessageID.description))

    override var reasonTitleError: String? = null
        get() = field
        set(value) {
            setState {
                reasonTitle = reasonTitle.copy(errorText = value)
            }
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var entity: LeavingReason? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        mPresenter = LeavingReasonEditPresenter(this, arguments, this,
            this,di)
        setEditTitle(MessageID.new_leaving_reason, MessageID.edit_leaving_reason)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {
        umGridContainer {
            css {
                +fieldsOnlyFormScreen
            }

            umItem(GridSize.cells12){
                umTextField(label = "${reasonTitle.text}",
                    helperText = reasonTitle.errorText,
                    value = entity?.leavingReasonTitle,
                    error = reasonTitle.error,
                    disabled = !fieldsEnabled,
                    variant = FormControlVariant.outlined,
                    onChange = {
                        setState {
                            entity?.leavingReasonTitle = it
                            reasonTitleError = null
                        }
                    })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
        reasonTitleError = null
    }

}