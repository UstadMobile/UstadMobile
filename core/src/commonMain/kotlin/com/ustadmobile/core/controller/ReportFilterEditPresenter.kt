package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.util.*
import com.ustadmobile.core.util.ext.OUTCOME_TO_MESSAGE_ID_MAP
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.view.ContentEntryList2View
import com.ustadmobile.core.view.LeavingReasonListView
import com.ustadmobile.core.view.ReportFilterEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class ReportFilterEditPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: ReportFilterEditView,
    di: DI,
    lifecycleOwner: LifecycleOwner
) : UstadEditPresenter<ReportFilterEditView, ReportFilter>(context, arguments, view, di, lifecycleOwner) {
    private val fieldRequiredText = systemImpl.getString(MessageID.field_required_prompt, context)

    private val uidhelperDeferred = CompletableDeferred<Boolean>()

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    enum class FieldOption(val optionVal: Int, val messageId: Int) {
        PERSON_GENDER(ReportFilter.FIELD_PERSON_GENDER, MessageID.field_person_gender),
        PERSON_AGE(ReportFilter.FIELD_PERSON_AGE, MessageID.field_person_age),
        CONTENT_COMPLETION(ReportFilter.FIELD_CONTENT_COMPLETION, MessageID.field_content_completion),
        CONTENT_ENTRY(ReportFilter.FIELD_CONTENT_ENTRY, MessageID.field_content_entry),
        CONTENT_PROGRESS(ReportFilter.FIELD_CONTENT_PROGRESS, MessageID.field_content_progress),
        ATTENDANCE_PERCENTAGE(ReportFilter.FIELD_ATTENDANCE_PERCENTAGE, MessageID.field_attendance_percentage),
        ENROLMENT_OUTCOME(ReportFilter.FIELD_CLAZZ_ENROLMENT_OUTCOME, MessageID.class_enrolment_outcome),
        ENROLMENT_LEAVING_REASON(ReportFilter.FIELD_CLAZZ_ENROLMENT_LEAVING_REASON, MessageID.class_enrolment_leaving)
    }

    class FieldMessageIdOption(day: FieldOption, context: Any, di: DI)
        : MessageIdOption(day.messageId, context, day.optionVal, di = di)

    enum class ConditionOption(val optionVal: Int, val messageId: Int) {
        IS_CONDITION(ReportFilter.CONDITION_IS, MessageID.condition_is),
        IS_NOT_CONDITION(ReportFilter.CONDITION_IS_NOT, MessageID.condition_is_not),
        GREATER_THAN_CONDITION(ReportFilter.CONDITION_GREATER_THAN, MessageID.condition_greater_than),
        LESS_THAN_CONDITION(ReportFilter.CONDITION_LESS_THAN, MessageID.condition_less_than),
        BETWEEN_CONDITION(ReportFilter.CONDITION_BETWEEN, MessageID.condition_between),
        IN_LIST_CONDITION(ReportFilter.CONDITION_IN_LIST, MessageID.condition_in_list),
        NOT_IN_LIST_CONDITION(ReportFilter.CONDITION_NOT_IN_LIST, MessageID.condition_not_in_list)
    }

    class ConditionMessageIdOption(day: ConditionOption, context: Any, di: DI)
        : MessageIdOption(day.messageId, context, day.optionVal, di = di)

    enum class ContentCompletionStatusOption(val optionVal: Int, val messageId: Int) {
        COMPLETED(StatementEntity.CONTENT_COMPLETE, MessageID.completed),
        PASSED(StatementEntity.CONTENT_PASSED, MessageID.passed),
        FAILED(StatementEntity.CONTENT_FAILED, MessageID.failed)
    }

    class ContentCompletionStatusMessageIdOption(day: ContentCompletionStatusOption, context: Any, di: DI)
        : MessageIdOption(day.messageId, context, day.optionVal, di)


    enum class FilterValueType {
        DROPDOWN,
        INTEGER,
        BETWEEN,
        LIST
    }

    private val uidAndLabelOneToManyHelper = DefaultOneToManyJoinEditHelper(
            UidAndLabel::uid, "state_uid_list",
            ListSerializer(UidAndLabel.serializer()),
            ListSerializer(UidAndLabel.serializer()), this, di, UidAndLabel::class) { uid = it }

    private fun handleAddOrEditUidAndLabel(entry: UidAndLabel) {
        GlobalScope.launch(doorMainDispatcher()) {
            uidhelperDeferred.await()
            uidAndLabelOneToManyHelper.onEditResult(entry)
        }
    }

    fun handleRemoveUidAndLabel(entry: UidAndLabel) {
        uidAndLabelOneToManyHelper.onDeactivateEntity(entry)
    }

    fun clearUidAndLabelList() {
        uidAndLabelOneToManyHelper.entitiesToInsert
        uidAndLabelOneToManyHelper.liveList.getValue()?.forEach {
            uidAndLabelOneToManyHelper.onDeactivateEntity(it)
        }
    }


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.fieldOptions = FieldOption.values().map { FieldMessageIdOption(it, context, di) }
        view.uidAndLabelList = uidAndLabelOneToManyHelper.liveList
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ReportFilter? {
        super.onLoadFromJson(bundle)
        val entityJsonStr = bundle[ARG_ENTITY_JSON] ?: ""

        val entity = safeParse(di, ReportFilter.serializer(), entityJsonStr)

        if (entity.reportFilterField != 0) {
            handleFieldOptionSelected(FieldOption.values().map { FieldMessageIdOption(it, context, di) }.find { it.code == entity.reportFilterField } as MessageIdOption)
        }
        if (entity.reportFilterCondition != 0) {
            handleConditionOptionSelected(ConditionOption.values().map { ConditionMessageIdOption(it, context, di) }.find { it.code == entity.reportFilterCondition } as MessageIdOption)
        }
        uidAndLabelOneToManyHelper.onLoadFromJsonSavedState(bundle)

        GlobalScope.launch(doorMainDispatcher()) {

            if (entity.reportFilterField == ReportFilter.FIELD_CONTENT_ENTRY) {
                if (entity.reportFilterValue != null && entity.reportFilterValue?.isNotEmpty() == true) {
                    val entries = withTimeoutOrNull(2000) {
                        db.contentEntryDao.getContentEntryFromUids(
                                entity.reportFilterValue?.split(", ")
                                        ?.map { it.toLong() }
                                        ?: listOf())
                    } ?: listOf()
                    uidAndLabelOneToManyHelper.liveList.postValue(entries)

                }
            }else if(entity.reportFilterField == ReportFilter.FIELD_CLAZZ_ENROLMENT_LEAVING_REASON){
                if (entity.reportFilterValue != null && entity.reportFilterValue?.isNotEmpty() == true) {
                    val reasons = withTimeoutOrNull(2000){
                        db.leavingReasonDao.getReasonsFromUids(entity.reportFilterValue?.split(", ")
                                ?.map { it.toLong() }
                                ?: listOf())
                    } ?: listOf()
                    uidAndLabelOneToManyHelper.liveList.postValue(reasons)
                }

            }
            uidhelperDeferred.complete(true)
        }

        return entity
    }


    fun handleFieldOptionSelected(fieldOption: IdOption) {
        when (fieldOption.optionId) {
            ReportFilter.FIELD_PERSON_GENDER -> {

                view.conditionsOptions = listOf(ConditionOption.IS_CONDITION,
                        ConditionOption.IS_NOT_CONDITION).map { ConditionMessageIdOption(it, context, di) }

                view.valueType = FilterValueType.DROPDOWN
                view.dropDownValueOptions = genderMap
                        .map { MessageIdOption(it.value, context, it.key, di) }
            }

            ReportFilter.FIELD_PERSON_AGE -> {

                view.conditionsOptions = listOf(ConditionOption.GREATER_THAN_CONDITION,
                        ConditionOption.LESS_THAN_CONDITION, ConditionOption.BETWEEN_CONDITION
                ).map {
                    ConditionMessageIdOption(it, context, di)
                }
                view.valueType = FilterValueType.INTEGER

            }
            ReportFilter.FIELD_CONTENT_COMPLETION -> {

                view.conditionsOptions = listOf(ConditionOption.IS_CONDITION).map { ConditionMessageIdOption(it, context, di) }
                view.valueType = FilterValueType.DROPDOWN
                view.dropDownValueOptions = ContentCompletionStatusOption.values()
                        .map { ContentCompletionStatusMessageIdOption(it, context, di) }
            }
            ReportFilter.FIELD_CONTENT_ENTRY -> {

                view.conditionsOptions = listOf(ConditionOption.IN_LIST_CONDITION,
                        ConditionOption.NOT_IN_LIST_CONDITION).map { ConditionMessageIdOption(it, context, di) }
                view.valueType = FilterValueType.LIST
                view.createNewFilter = systemImpl.getString(MessageID.add_content_filter, context)

            }
            ReportFilter.FIELD_ATTENDANCE_PERCENTAGE, ReportFilter.FIELD_CONTENT_PROGRESS -> {

                view.conditionsOptions = listOf(ConditionOption.BETWEEN_CONDITION)
                        .map { ConditionMessageIdOption(it, context, di) }
                view.valueType = FilterValueType.BETWEEN
            }
            ReportFilter.FIELD_CLAZZ_ENROLMENT_OUTCOME -> {
                view.conditionsOptions = listOf(ConditionOption.IS_CONDITION,
                        ConditionOption.IS_NOT_CONDITION).map { ConditionMessageIdOption(it, context, di) }
                view.valueType = FilterValueType.DROPDOWN
                view.dropDownValueOptions = OUTCOME_TO_MESSAGE_ID_MAP.map {
                    MessageIdOption(it.value, context, it.key, di) }
            }
            ReportFilter.FIELD_CLAZZ_ENROLMENT_LEAVING_REASON -> {
                view.conditionsOptions = listOf(ConditionOption.IN_LIST_CONDITION,
                        ConditionOption.NOT_IN_LIST_CONDITION).map { ConditionMessageIdOption(it, context, di) }
                view.valueType = FilterValueType.LIST
                view.createNewFilter = systemImpl.getString(MessageID.add_leaving_reason, context)
            }
        }

    }

    fun handleConditionOptionSelected(conditionOption: IdOption) {
        when (conditionOption.optionId) {
            ReportFilter.CONDITION_GREATER_THAN, ReportFilter.CONDITION_LESS_THAN -> {
                view.valueType = FilterValueType.INTEGER
            }
            ReportFilter.CONDITION_BETWEEN -> {
                view.valueType = FilterValueType.BETWEEN
            }
        }
    }


    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = entity
        if (entityVal?.reportFilterField == ReportFilter.FIELD_CONTENT_ENTRY ||
                entityVal?.reportFilterField == ReportFilter.FIELD_CLAZZ_ENROLMENT_LEAVING_REASON) {
            entityVal.reportFilterValue = uidAndLabelOneToManyHelper.liveList.getValue()
                    ?.joinToString { it.uid.toString() }
        }
        savedState.putEntityAsJson(ARG_ENTITY_JSON, json, ReportFilter.serializer(), entityVal)
    }


    override fun onLoadDataComplete() {
        super.onLoadDataComplete()

        observeSavedStateResult(
            RESULT_LEAVING_REASON_KEY,
            ListSerializer(LeavingReason.serializer()), LeavingReason::class) {
            val reason = it.firstOrNull() ?: return@observeSavedStateResult
            handleAddOrEditUidAndLabel(UidAndLabel().apply {
                uid = reason.leavingReasonUid
                labelName = reason.leavingReasonTitle
            })
        }

        observeSavedStateResult(
            RESULT_CONTENT_KEY,
            ListSerializer(ContentEntry.serializer()), ContentEntry::class) {
            val entry = it.firstOrNull() ?: return@observeSavedStateResult
            handleAddOrEditUidAndLabel(UidAndLabel().apply {
                uid = entry.contentEntryUid
                labelName = entry.title
            })
        }
    }

    fun handleAddLeavingReasonClicked(){
        navigateForResult(
            NavigateForResultOptions(
                this, null,
                LeavingReasonListView.VIEW_NAME,
                LeavingReason::class,
                LeavingReason.serializer(),
                RESULT_LEAVING_REASON_KEY
            )
        )
    }

    fun handleAddContentClicked(){
        navigateForResult(
            NavigateForResultOptions(
                this, null,
                ContentEntryList2View.VIEW_NAME,
                ContentEntry::class,
                ContentEntry.serializer(),
                RESULT_CONTENT_KEY,
                arguments = mutableMapOf(ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_OPTION to
                        ContentEntryList2View.ARG_DISPLAY_CONTENT_BY_PARENT,
                    UstadView.ARG_PARENT_ENTRY_UID to UstadView.MASTER_SERVER_ROOT_ENTRY_UID.toString())
            )
        )
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
        if (entity.reportFilterField == ReportFilter.FIELD_CONTENT_ENTRY ||
                entity.reportFilterField == ReportFilter.FIELD_CLAZZ_ENROLMENT_LEAVING_REASON) {
            entity.reportFilterValue = uidAndLabelOneToManyHelper.liveList.getValue()
                    ?.joinToString { it.uid.toString() }
        }

        if (entity.reportFilterDropDownValue == 0 && entity.reportFilterValue.isNullOrBlank() &&
                (entity.reportFilterValueBetweenX.isNullOrEmpty() ||
                        entity.reportFilterValueBetweenY.isNullOrEmpty())) {
            view.valuesErrorText = fieldRequiredText
            return
        } else {
            view.valuesErrorText = null
        }

        val serializedResult = safeStringify(di, ListSerializer(ReportFilter.serializer()),
            listOf(entity))
        finishWithResult(serializedResult)
    }

    companion object {

        val genderMap = PersonConstants.GENDER_MESSAGE_ID_MAP

        const val RESULT_CONTENT_KEY = "Content"

        const val RESULT_LEAVING_REASON_KEY = "LeavingReason"

    }

}