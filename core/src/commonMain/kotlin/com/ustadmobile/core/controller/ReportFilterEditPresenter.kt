package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.createNewSchoolAndGroups
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.ReportFilterEditView
import com.ustadmobile.core.view.ReportFilterEditView.Companion.ARG_REPORT_FILTER
import com.ustadmobile.core.view.UstadEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ReportFilter
import kotlinx.atomicfu.AtomicLong
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.withContext
import org.kodein.di.DI


class ReportFilterEditPresenter(context: Any,
                                arguments: Map<String, String>, view: ReportFilterEditView,
                                di: DI,
                                lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<ReportFilterEditView, ReportFilter>(context, arguments, view, di, lifecycleOwner) {

    val reportFilterUids = atomic(1L)

    val fieldRequiredText = systemImpl.getString(MessageID.field_required_prompt, context)

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    enum class FieldOption(val optionVal: Int, val messageId: Int) {
        PERSON_GENDER(ReportFilter.FIELD_PERSON_GENDER, MessageID.field_person_gender),
        PERSON_AGE(ReportFilter.FIELD_PERSON_AGE, MessageID.field_person_age)
    }

    class FieldMessageIdOption(day: FieldOption, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)

    enum class ConditionOption(val optionVal: Int, val messageId: Int) {
        IS_CONDITION(ReportFilter.CONDITION_IS, MessageID.condition_is),
        IS_NOT_CONDITION(ReportFilter.CONDITION_IS_NOT, MessageID.condition_is_not),
        GREATER_THAN_CONDITION(ReportFilter.CONDITION_GREATER_THAN, MessageID.condition_greater_than),
        LESS_THAN_CONDITION(ReportFilter.CONDITION_LESS_THAN, MessageID.condition_less_than)
        //WITHIN_RANGE_CONDITION(ReportFilter.CONDITION_WITHIN_RANGE, MessageID.class_id),
    }

    class ConditionMessageIdOption(day: ConditionOption, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)

    enum class ValueOption(val optionVal: Int, val messageId: Int) {

    }



    class ValueMessageIdOption(day: ValueOption, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)


    enum class FilterValueType {
        DROPDOWN,
        INTEGER,
        RANGE
    }


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.fieldOptions = FieldOption.values().map { FieldMessageIdOption(it, context) }
        view.conditionsOptions = ConditionOption.values().map { ConditionMessageIdOption(it, context) }
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ReportFilter? {
        val entityJsonStr = arguments[ARG_ENTITY_JSON] ?: ""

        val entity = safeParse(di, ReportFilter.serializer(), entityJsonStr)

        if(entity.reportFilterField != 0){
            handleFieldOptionSelected(FieldOption.values().map { FieldMessageIdOption(it, context) }.find { it.code == entity.reportFilterField } as MessageIdOption)
        }
        if(entity.reportFilterCondition != 0){
            handleConditionOptionSelected(ConditionOption.values().map { ConditionMessageIdOption(it, context) }.find { it.code == entity.reportFilterCondition } as MessageIdOption)
        }

        return entity
    }


    fun handleFieldOptionSelected(fieldOption: MessageIdOption) {
        when(fieldOption.code){
            ReportFilter.FIELD_PERSON_GENDER -> {

                view.conditionsOptions = listOf(ConditionOption.IS_CONDITION,
                        ConditionOption.IS_NOT_CONDITION).map { ConditionMessageIdOption(it, context) }

                view.valueType = FilterValueType.DROPDOWN
                view.dropDownValueOptions = genderMap
                        .map { MessageIdOption(it.value, context, it.key) }
            }

            ReportFilter.FIELD_PERSON_AGE -> {
                view.conditionsOptions = listOf(ConditionOption.GREATER_THAN_CONDITION,
                        ConditionOption.LESS_THAN_CONDITION).map { ConditionMessageIdOption(it, context) }
                view.valueType = FilterValueType.INTEGER
            }
        }

    }

    fun handleConditionOptionSelected(conditionOption: MessageIdOption) {

    }


    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null, entityVal)
    }

    override fun handleClickSave(entity: ReportFilter) {
        if (entity.reportFilterField == 0) {
            view.fieldErrorText = fieldRequiredText
            return
        } else {
            view.fieldErrorText = null
        }
        if (entity.reportFilterCondition == 0) {
            view.conditionsErrorText = fieldRequiredText
            return
        } else {
            view.conditionsErrorText = null
        }
        if (entity.reportFilterDropDownValue == 0 && entity.reportFilterValue.isNullOrBlank()) {

            // if gender, value can be 0 for unset gender
            if(entity.reportFilterField == ReportFilter.FIELD_PERSON_GENDER){
                view.valuesErrorText = null
            }else{
                view.valuesErrorText = fieldRequiredText
                return
            }
        } else {
            view.valuesErrorText = null
        }

        entity.reportFilterUid = reportFilterUids.incrementAndGet()

        view.finishWithResult(listOf(entity))
    }

    companion object{

        val genderMap = mapOf(Person.GENDER_UNSET to MessageID.unset)
                .plus(PersonConstants.GENDER_MESSAGE_ID_MAP)

    }

}