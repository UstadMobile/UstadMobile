package com.ustadmobile.view

import com.ustadmobile.util.FieldLabel
import com.ustadmobile.core.controller.ReportFilterEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.util.IdOption
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.view.ReportFilterEditView
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.ObserverFnWrapper
import com.ustadmobile.lib.db.entities.ReportFilter
import com.ustadmobile.lib.db.entities.UidAndLabel
import com.ustadmobile.mui.components.*
import com.ustadmobile.util.StyleManager.defaultMarginTop
import com.ustadmobile.util.StyleManager.defaultPaddingTop
import com.ustadmobile.util.StyleManager.fieldsOnlyFormScreen
import com.ustadmobile.util.StyleManager.horizontalList
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.Util
import com.ustadmobile.util.ext.currentBackStackEntrySavedStateMap
import com.ustadmobile.view.ext.renderCreateNewItemOnList
import com.ustadmobile.view.ext.renderListItemWithTitleDescriptionAndRightAction
import com.ustadmobile.view.ext.umGridContainer
import com.ustadmobile.view.ext.umItem
import react.RBuilder
import react.dom.html.InputType
import react.setState
import styled.css

class ReportFilterEditComponent (mProps: UmProps): UstadEditComponent<ReportFilter>(mProps),
    ReportFilterEditView {

    private var mPresenter: ReportFilterEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ReportFilter>?
        get() = mPresenter


    private var fieldLabel = FieldLabel(text = getString(MessageID.report_filter_edit_field))

    private var conditionLabel = FieldLabel(text = getString(MessageID.report_filter_edit_condition))

    private var valueLabel = FieldLabel(text = getString(MessageID.report_filter_edit_values))

    private var fromLabel = FieldLabel(text = getString(MessageID.from))

    private var toLabel = FieldLabel(text = getString(MessageID.to))

    override var fieldOptions: List<ReportFilterEditPresenter.FieldMessageIdOption>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var conditionsOptions: List<ReportFilterEditPresenter.ConditionMessageIdOption>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var dropDownValueOptions: List<MessageIdOption>? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override var valueType: ReportFilterEditPresenter.FilterValueType? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }

    override var fieldErrorText: String? = null
        get() = field
        set(value) {
            setState {
                fieldLabel = fieldLabel.copy(errorText = value)
            }
        }


    override var conditionsErrorText: String? = null
        get() = field
        set(value) {
            setState {
                conditionLabel = conditionLabel.copy(errorText = value)
            }
        }

    override var valuesErrorText: String? = null
        get() = field
        set(value) {
            setState {
                valueLabel = valueLabel.copy(errorText = value)
            }
        }

    private var uidAndLabels: List<UidAndLabel> = listOf()

    private val uidAndLabelFilterItemObserver = ObserverFnWrapper<List<UidAndLabel>?> {
        if(it == null) return@ObserverFnWrapper

        setState{
            uidAndLabels = it
        }
    }

    override var uidAndLabelList: DoorLiveData<List<UidAndLabel>>? = null
        set(value) {
            field?.removeObserver(uidAndLabelFilterItemObserver)
            field = value
            field?.observe(this, uidAndLabelFilterItemObserver)
        }

    override var createNewFilter: String? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            setState {
                field = value
            }
        }
    override var entity: ReportFilter? = null
        get() = field
        set(value) {
            setState {
                field = value
            }
        }


    override fun onCreateView() {
        super.onCreateView()
        setEditTitle(MessageID.edit_filters, MessageID.edit_filters)
        mPresenter = ReportFilterEditPresenter(this, arguments, this,
            di, this)
        mPresenter?.onCreate(navController.currentBackStackEntrySavedStateMap())
    }

    override fun RBuilder.render() {
        umGridContainer {
            css{
                +fieldsOnlyFormScreen
                +defaultPaddingTop
            }

            umItem(GridSize.cells12) {
                umTextFieldSelect(
                    "${fieldLabel.text}",
                    entity?.reportFilterField.toString(),
                    fieldLabel.errorText ?: "",
                    error = fieldLabel.error,
                    values = fieldOptions?.map {
                        Pair(it.code.toString(), it.toString())
                    }?.toList(),
                    onChange = {
                        if(entity?.reportFilterField != it.toInt()){
                            mPresenter?.clearUidAndLabelList()
                        }

                        setState {
                            entity?.reportFilterField = it.toInt()
                            fieldErrorText = null
                        }

                        val option = IdOption("", it.toInt())
                        mPresenter?.handleFieldOptionSelected(option)
                        mPresenter?.handleConditionOptionSelected(option)
                    }
                )

                umGridContainer(columnSpacing =GridSpacing.spacing4) {
                    val fullWidth = valueType != ReportFilterEditPresenter.FilterValueType.INTEGER
                            && valueType != ReportFilterEditPresenter.FilterValueType.DROPDOWN
                    umItem(GridSize.cells12, if(fullWidth) GridSize.cells12 else GridSize.cells6){
                        umTextFieldSelect(
                            "${conditionLabel.text}",
                            entity?.reportFilterCondition.toString(),
                            conditionLabel.errorText ?: "",
                            error = conditionLabel.error,
                            values = conditionsOptions?.map {
                                Pair(it.code.toString(), it.toString())
                            }?.toList(),
                            onChange = {
                                setState {
                                    entity?.reportFilterCondition = it.toInt()
                                    conditionsErrorText = null
                                }
                            }
                        )
                    }

                    umItem(GridSize.cells12, GridSize.cells6){

                        if(valueType == ReportFilterEditPresenter.FilterValueType.INTEGER){
                            umTextField(label = "${valueLabel.text}",
                                helperText = valueLabel.errorText,
                                value = entity?.reportFilterValue,
                                error = valueLabel.error,
                                fullWidth = true,
                                type = InputType.number,
                                disabled = !fieldsEnabled,
                                variant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.reportFilterValue = it
                                    }
                                })
                        }

                        if(valueType == ReportFilterEditPresenter.FilterValueType.DROPDOWN){
                            umTextFieldSelect(
                                "${valueLabel.text}",
                                entity?.reportFilterDropDownValue.toString(),
                                valueLabel.errorText ?: "",
                                error = valueLabel.error,
                                values = dropDownValueOptions?.map {
                                    Pair(it.code.toString(), it.toString())
                                }?.toList(),
                                onChange = {
                                    setState {
                                        entity?.reportFilterDropDownValue = it.toInt()
                                        valuesErrorText = null
                                    }
                                }
                            )
                        }
                    }
                }

                if(valueType  == ReportFilterEditPresenter.FilterValueType.BETWEEN){
                    umGridContainer(columnSpacing =GridSpacing.spacing4) {
                        umItem(GridSize.cells12, GridSize.cells6) {
                            umTextField(label = "${fromLabel.text}",
                                helperText = fromLabel.errorText,
                                value = entity?.reportFilterValueBetweenX,
                                error = fromLabel.error,
                                fullWidth = true,
                                type = InputType.number,
                                disabled = !fieldsEnabled,
                                variant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.reportFilterValueBetweenX = it
                                    }
                                })
                        }

                        umItem(GridSize.cells12, GridSize.cells6) {
                            umTextField(label = "${toLabel.text}",
                                helperText = toLabel.errorText,
                                value = entity?.reportFilterValueBetweenY,
                                error = toLabel.error,
                                fullWidth = true,
                                disabled = !fieldsEnabled,
                                type = InputType.number,
                                variant = FormControlVariant.outlined,
                                onChange = {
                                    setState {
                                        entity?.reportFilterValueBetweenY = it
                                    }
                                })
                        }
                    }
                }

                if(valueType == ReportFilterEditPresenter.FilterValueType.LIST){
                    if(uidAndLabels.isNotEmpty()){
                        umItem(GridSize.cells12) {
                            css{
                                +horizontalList
                                +defaultPaddingTop
                            }
                            for(uidLabel in uidAndLabels){
                                umListItem(button = true) {
                                    renderListItemWithTitleDescriptionAndRightAction(
                                        uidLabel.labelName ?: "", "delete",
                                        withAction = true){
                                        Util.stopEventPropagation(it)
                                        mPresenter?.handleRemoveUidAndLabel(uidLabel)
                                    }
                                }
                            }
                        }
                    }

                    umItem(GridSize.cells12){
                        css(defaultMarginTop)
                        umListItem(button = true) {
                            attrs.onClick = {
                                Util.stopEventPropagation(it)
                                if(entity?.reportFilterField == ReportFilter.FIELD_CONTENT_ENTRY) {
                                    mPresenter?.handleAddContentClicked()
                                }else if(entity?.reportFilterField == ReportFilter.FIELD_CLAZZ_ENROLMENT_LEAVING_REASON){
                                    mPresenter?.handleAddLeavingReasonClicked()
                                }
                            }
                            renderCreateNewItemOnList(createNewFilter ?: "")
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        entity = null
    }

}