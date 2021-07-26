package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.DefaultOneToManyJoinEditHelper
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.ext.OUTCOME_TO_MESSAGE_ID_MAP
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.ReportFilterEditView
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.builtins.ListSerializer
import org.kodein.di.DI


class ReportFilterEditPresenter(context: Any,
                                arguments: Map<String, String>, view: ReportFilterEditView,
                                di: DI,
                                lifecycleOwner: DoorLifecycleOwner)
    : UstadEditPresenter<ReportFilterEditView, ReportFilter>(context, arguments, view, di, lifecycleOwner) {

    val fieldRequiredText = systemImpl.getString(MessageID.field_required_prompt, context)

    val uidhelperDeferred = CompletableDeferred<Boolean>()

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.JSON

    enum class FieldOption(val optionVal: Int, val messageId: Int) {
        LE_GENDER(ReportFilter.FIELD_LE_GENDER, MessageID.gender_literal),
        SALE_AMOUNT(ReportFilter.FIELD_SALE_AMOUNT, MessageID.each_sales_total_afs),
        LOCATION(ReportFilter.FIELD_LOCATION, MessageID.province),
        CATEGORY(ReportFilter.FIELD_CATEGORY, MessageID.category),
        LE(ReportFilter.FIELD_LE, MessageID.le),
        WE(ReportFilter.FIELD_WE, MessageID.we)

    }

    class FieldMessageIdOption(day: FieldOption, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)

    enum class ConditionOption(val optionVal: Int, val messageId: Int) {
        IS_CONDITION(ReportFilter.CONDITION_IS, MessageID.condition_is),
        IS_NOT_CONDITION(ReportFilter.CONDITION_IS_NOT, MessageID.condition_is_not),
        GREATER_THAN_CONDITION(ReportFilter.CONDITION_GREATER_THAN, MessageID.condition_greater_than),
        LESS_THAN_CONDITION(ReportFilter.CONDITION_LESS_THAN, MessageID.condition_less_than),
        BETWEEN_CONDITION(ReportFilter.CONDITION_BETWEEN, MessageID.condition_between),
        IN_LIST_CONDITION(ReportFilter.CONDITION_IN_LIST, MessageID.condition_in_list),
        NOT_IN_LIST_CONDITION(ReportFilter.CONDITION_NOT_IN_LIST, MessageID.condition_not_in_list)
    }

    class ConditionMessageIdOption(day: ConditionOption, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)

    enum class ContentCompletionStatusOption(val optionVal: Int, val messageId: Int) {
        COMPLETED(StatementEntity.CONTENT_COMPLETE, MessageID.completed),
        PASSED(StatementEntity.CONTENT_PASSED, MessageID.passed),
        FAILED(StatementEntity.CONTENT_FAILED, MessageID.failed)
    }

    class ContentCompletionStatusMessageIdOption(day: ContentCompletionStatusOption, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)


    enum class FilterValueType {
        DROPDOWN,
        INTEGER,
        BETWEEN,
        LIST
    }

    private val uidAndLabelOneToManyHelper = DefaultOneToManyJoinEditHelper(
            UidAndLabel::uid, "state_uid_list",
            ListSerializer(UidAndLabel.serializer()),
            ListSerializer(UidAndLabel.serializer()), this, UidAndLabel::class) { uid = it }

    fun handleAddOrEditUidAndLabel(entry: UidAndLabel) {
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
        view.fieldOptions = FieldOption.values().map { FieldMessageIdOption(it, context) }
        view.uidAndLabelList = uidAndLabelOneToManyHelper.liveList
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ReportFilter? {
        super.onLoadFromJson(bundle)
        val entityJsonStr = bundle[ARG_ENTITY_JSON] ?: ""

        val entity = safeParse(di, ReportFilter.serializer(), entityJsonStr)

        if (entity.reportFilterField != 0) {
            handleFieldOptionSelected(FieldOption.values().map { FieldMessageIdOption(it, context) }.find { it.code == entity.reportFilterField } as MessageIdOption)
        }
        if (entity.reportFilterCondition != 0) {
            handleConditionOptionSelected(ConditionOption.values().map { ConditionMessageIdOption(it, context) }.find { it.code == entity.reportFilterCondition } as MessageIdOption)
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
                    uidAndLabelOneToManyHelper.liveList.sendValue(entries)

                }
            }else if(entity.reportFilterField == ReportFilter.FIELD_CLAZZ_ENROLMENT_LEAVING_REASON){
                if (entity.reportFilterValue != null && entity.reportFilterValue?.isNotEmpty() == true) {
                    val reasons = withTimeoutOrNull(2000){
                        db.leavingReasonDao.getReasonsFromUids(entity.reportFilterValue?.split(", ")
                                ?.map { it.toLong() }
                                ?: listOf())
                    } ?: listOf()
                    uidAndLabelOneToManyHelper.liveList.sendValue(reasons)
                }

            }else if (entity.reportFilterField == ReportFilter.FIELD_LE) {
                if (entity.reportFilterValue != null && entity.reportFilterValue?.isNotEmpty() == true) {
                    val entries = withTimeoutOrNull(2000) {
                        db.personDao.getPeopleFromUids(
                                entity.reportFilterValue?.split(", ")
                                        ?.map { it.toLong() }
                                        ?: listOf())
                    } ?: listOf()
                    uidAndLabelOneToManyHelper.liveList.sendValue(entries)

                }
            }else if (entity.reportFilterField == ReportFilter.FIELD_WE) {
                if (entity.reportFilterValue != null && entity.reportFilterValue?.isNotEmpty() == true) {
                    val entries = withTimeoutOrNull(2000) {
                        db.personDao.getPeopleFromUids(
                                entity.reportFilterValue?.split(", ")
                                        ?.map { it.toLong() }
                                        ?: listOf())
                    } ?: listOf()
                    uidAndLabelOneToManyHelper.liveList.sendValue(entries)

                }
            }else if (entity.reportFilterField == ReportFilter.FIELD_LOCATION) {
                if (entity.reportFilterValue != null && entity.reportFilterValue?.isNotEmpty() == true) {
                    val entries = withTimeoutOrNull(2000) {
                        db.locationDao.getLocationsFromUids(
                                entity.reportFilterValue?.split(", ")
                                        ?.map { it.toLong() }
                                        ?: listOf())
                    } ?: listOf()
                    uidAndLabelOneToManyHelper.liveList.sendValue(entries)

                }
            }else if (entity.reportFilterField == ReportFilter.FIELD_CATEGORY) {
                if (entity.reportFilterValue != null && entity.reportFilterValue?.isNotEmpty() == true) {
                    val entries = withTimeoutOrNull(2000) {
                        db.categoryDao.getCategoriesFromUids(
                                entity.reportFilterValue?.split(", ")
                                        ?.map { it.toLong() }
                                        ?: listOf())
                    } ?: listOf()
                    uidAndLabelOneToManyHelper.liveList.sendValue(entries)

                }
            }

            uidhelperDeferred.complete(true)
        }

        return entity
    }


    fun handleFieldOptionSelected(fieldOption: IdOption) {
        when (fieldOption.optionId) {
            ReportFilter.FIELD_LE_GENDER -> {

                view.conditionsOptions = listOf(ConditionOption.IS_CONDITION,
                        ConditionOption.IS_NOT_CONDITION).map { ConditionMessageIdOption(it, context) }

                view.valueType = FilterValueType.DROPDOWN
                view.dropDownValueOptions = genderMap
                        .map { MessageIdOption(it.value, context, it.key) }
            }

            ReportFilter.FIELD_SALE_AMOUNT -> {

                view.conditionsOptions = listOf(ConditionOption.GREATER_THAN_CONDITION,
                        ConditionOption.LESS_THAN_CONDITION, ConditionOption.BETWEEN_CONDITION).map { ConditionMessageIdOption(it, context) }
                view.valueType = FilterValueType.INTEGER

            }

            ReportFilter.FIELD_LOCATION  -> {

                view.conditionsOptions = listOf(ConditionOption.IN_LIST_CONDITION,
                        ConditionOption.NOT_IN_LIST_CONDITION).map { ConditionMessageIdOption(it, context) }
                view.valueType = FilterValueType.LIST
                view.createNewFilter = systemImpl.getString(MessageID.locations, context)

            }
            ReportFilter.FIELD_CATEGORY  -> {

                view.conditionsOptions = listOf(ConditionOption.IN_LIST_CONDITION,
                        ConditionOption.NOT_IN_LIST_CONDITION).map { ConditionMessageIdOption(it, context) }
                view.valueType = FilterValueType.LIST
                view.createNewFilter = systemImpl.getString(MessageID.categories, context)

            }
            ReportFilter.FIELD_LE  -> {

                view.conditionsOptions = listOf(ConditionOption.IN_LIST_CONDITION,
                        ConditionOption.NOT_IN_LIST_CONDITION).map { ConditionMessageIdOption(it, context) }
                view.valueType = FilterValueType.LIST
                view.createNewFilter = systemImpl.getString(MessageID.le, context)

            }
            ReportFilter.FIELD_WE  -> {

                view.conditionsOptions = listOf(ConditionOption.IN_LIST_CONDITION,
                        ConditionOption.NOT_IN_LIST_CONDITION).map { ConditionMessageIdOption(it, context) }
                view.valueType = FilterValueType.LIST
                view.createNewFilter = systemImpl.getString(MessageID.we, context)

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
                entityVal?.reportFilterField == ReportFilter.FIELD_CLAZZ_ENROLMENT_LEAVING_REASON ||
                entityVal?.reportFilterField == ReportFilter.FIELD_LE ||
                entityVal?.reportFilterField == ReportFilter.FIELD_WE ||
                entityVal?.reportFilterField == ReportFilter.FIELD_LOCATION ||
                entityVal?.reportFilterField == ReportFilter.FIELD_CATEGORY ){
            entityVal.reportFilterValue = uidAndLabelOneToManyHelper.liveList.getValue()
                    ?.joinToString { it.uid.toString() }
        }
        savedState.putEntityAsJson(ARG_ENTITY_JSON, ReportFilter.serializer(), entityVal)
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
                entity.reportFilterField == ReportFilter.FIELD_CLAZZ_ENROLMENT_LEAVING_REASON ||
                entity.reportFilterField == ReportFilter.FIELD_LE ||
                entity.reportFilterField == ReportFilter.FIELD_WE ||
                entity.reportFilterField == ReportFilter.FIELD_CATEGORY ||
                entity.reportFilterField == ReportFilter.FIELD_LOCATION ) {
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
        view.finishWithResult(listOf(entity))
    }

    companion object {

        val genderMap = PersonConstants.GENDER_MESSAGE_ID_MAP

    }

}