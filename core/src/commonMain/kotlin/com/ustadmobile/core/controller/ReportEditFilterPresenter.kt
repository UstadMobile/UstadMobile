package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ReportEditFilterView
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.lib.db.entities.ReportFilter
import org.kodein.di.DI


class ReportEditFilterPresenter(context: Any,
                                arguments: Map<String, String>, view: ReportEditFilterView,
                                di: DI,
                                lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<ReportEditFilterView, ReportFilter>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    enum class FieldTypeOption(val optionVal: Int, val messageId: Int) {

    }

    class FieldTypeMessageIdOption(day: FieldTypeOption, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)


    enum class ConditionTypeOption(val optionVal: Int, val messageId: Int) {

    }

    class ConditionTypeMessageIdOption(day: ConditionTypeOption, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)

    enum class ValueTypeOption(val optionVal: Int, val messageId: Int) {

    }

    class ValueTypeMessageIdOption(day: ValueTypeOption, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ReportFilter? {
        super.onLoadFromJson(bundle)

        return null
    }

    // based on enum selection
    fun handleFieldOptionSelected(fieldOption: String){
        // changes the drop down option of condition and values
    }


    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(UstadEditView.ARG_ENTITY_JSON, null, entityVal)
    }

    override fun handleClickSave(entity: ReportFilter) {
        TODO("Not yet implemented")
    }


}