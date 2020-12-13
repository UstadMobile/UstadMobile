package com.ustadmobile.port.android.view

import android.widget.AdapterView
import com.ustadmobile.core.controller.ReportFilterEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.ReportFilterEditView
import com.ustadmobile.lib.db.entities.ReportFilter

class ReportFilterEditFragment : UstadEditFragment<ReportFilter>(), ReportFilterEditView,
        DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<MessageIdOption> {

    private var mPresenter: ReportFilterEditPresenter? = null

    override val viewContext: Any
        get() = requireContext()

    override val mEditPresenter: UstadEditPresenter<*, ReportFilter>?
        get() = mPresenter


    override var conditionsOptions: List<ReportFilterEditPresenter.ConditionOption>?
        get() = TODO("Not yet implemented")
        set(value) {}
    override var dropDownValueOptions: List<ReportFilterEditPresenter.ValueOption>?
        get() = TODO("Not yet implemented")
        set(value) {}

    override var valueType: ReportFilterEditPresenter.FilterValueType
        get() = TODO("Not yet implemented")
        set(value) {}
    override var fieldOptions: List<ReportFilterEditPresenter.FieldOption>?
        get() = TODO("Not yet implemented")
        set(value) {}

    override var fieldsEnabled: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}


    override var entity: ReportFilter? = null
        get() = field
        set(value) {
            field = value
        }

    override fun onDropDownItemSelected(view: AdapterView<*>?, selectedOption: MessageIdOption) {
        TODO("Not yet implemented")
    }

    override fun onNoMessageIdOptionSelected(view: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}