package com.ustadmobile.port.android.view

import android.widget.AdapterView
import com.ustadmobile.core.controller.ReportEditFilterPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.ReportEditFilterView
import com.ustadmobile.lib.db.entities.ReportFilter

class ReportEditFilterFragment : UstadEditFragment<ReportFilter>(), ReportEditFilterView,
        DropDownListAutoCompleteTextView.OnDropDownListItemSelectedListener<MessageIdOption> {

    private var mPresenter: ReportEditFilterPresenter? = null

    override val viewContext: Any
        get() = requireContext()

    override val mEditPresenter: UstadEditPresenter<*, ReportFilter>?
        get() = mPresenter

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