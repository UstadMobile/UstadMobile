package com.ustadmobile.core.controller

import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ReportFilterEditView
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.ReportFilter
import org.kodein.di.DI


class ReportFilterEditPresenter(context: Any,
                                arguments: Map<String, String>, view: ReportFilterEditView,
                                di: DI,
                                lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<ReportFilterEditView, ReportFilter>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    enum class FieldOption(val optionVal: Int, val messageId: Int) {

    }

    class FieldMessageIdOption(day: FieldOption, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)


    enum class ConditionOption(val optionVal: Int, val messageId: Int) {

    }

    class ConditionTypeMessageIdOption(day: ConditionOption, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)

    enum class ValueOption(val optionVal: Int, val messageId: Int) {

    }

    class ValueTypeMessageIdOption(day: ValueOption, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)


    enum class FilterValueType{
        DROPDOWN,
        INTEGER
    }


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ReportFilter? {
        super.onLoadFromJson(bundle)

        return null
    }

    // based on enum selection
    fun handleFieldOptionSelected(fieldOption: FieldOption){
        // changes the drop down option of condition and values
    }

    fun handleConditionOptionSelected(conditionOption: ConditionOption){
        
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