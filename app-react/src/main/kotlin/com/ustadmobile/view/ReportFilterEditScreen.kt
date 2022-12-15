package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.ConditionConstants
import com.ustadmobile.core.impl.locale.entityconstants.ContentCompletionStatusConstants
import com.ustadmobile.core.impl.locale.entityconstants.FieldConstants
import com.ustadmobile.core.viewmodel.ReportFilterEditUiState
import com.ustadmobile.lib.db.entities.ReportFilter
import com.ustadmobile.lib.db.entities.UidAndLabel
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.mui.components.UstadMessageIdDropDownField
import com.ustadmobile.mui.components.UstadTextEditField
import csstype.px
import mui.icons.material.Add
import mui.icons.material.Delete
import mui.material.*
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.html.InputMode

external interface ReportFilterEditScreenProps : Props {

    var uiState: ReportFilterEditUiState

    var onClickNewItemFilter: () -> Unit

    var onReportFilterChanged: (ReportFilter?) -> Unit

    var onClickEditFilter: (UidAndLabel?) -> Unit

    var onClickRemoveFilter: (UidAndLabel?) -> Unit

}

val ReportFilterEditScreenPreview = FC<Props> {
    ReportFilterEditScreenComponent2 {
        uiState = ReportFilterEditUiState(
            uidAndLabelList = listOf(
                UidAndLabel().apply {
                    labelName = "First Filter"
                },
                UidAndLabel().apply {
                    labelName = "Second Filter"
                }
            ),
            createNewFilter = "Create new filter"
        )
    }
}

private val ReportFilterEditScreenComponent2 = FC<ReportFilterEditScreenProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            UstadMessageIdDropDownField {
                value = props.uiState.reportFilter?.reportFilterField ?: 0
                label = strings[MessageID.report_filter_edit_field]
                options = FieldConstants.FIELD_MESSAGE_IDS
                error = props.uiState.fieldError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onReportFilterChanged(
                        props.uiState.reportFilter?.shallowCopy {
                            reportFilterField = it?.value ?: 0
                    })
                }
            }

            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(10.px)

                UstadMessageIdDropDownField {
                    value = props.uiState.reportFilter?.reportFilterCondition ?: 0
                    label = strings[MessageID.report_filter_edit_condition]
                    options = ConditionConstants.CONDITION_MESSAGE_IDS
                    error = props.uiState.conditionsError
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onReportFilterChanged(
                            props.uiState.reportFilter?.shallowCopy {
                                reportFilterCondition = it?.value ?: 0
                            })
                    }
                }

                UstadMessageIdDropDownField {
                    value = props.uiState.reportFilter?.reportFilterDropDownValue ?: 0
                    label = strings[MessageID.report_filter_edit_values]
                    options = ContentCompletionStatusConstants.CONTENT_COMPLETION_STATUS_MESSAGE_IDS
                    error = props.uiState.valuesError
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onReportFilterChanged(
                            props.uiState.reportFilter?.shallowCopy {
                                reportFilterDropDownValue = it?.value ?: 0
                            })
                    }
                }
            }

            UstadTextEditField {
                value = props.uiState.reportFilter?.reportFilterValue ?: ""
                label = strings[MessageID.report_filter_edit_values]
                error = props.uiState.valuesError
                enabled = props.uiState.fieldsEnabled
                inputProps = {
                    it.inputMode = InputMode.numeric
                }
                onChange = {
                    props.onReportFilterChanged(
                        props.uiState.reportFilter?.shallowCopy {
                            reportFilterValue = it
                        })
                }
            }

            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(10.px)

                UstadTextEditField {
                    value = props.uiState.reportFilter?.reportFilterValueBetweenX ?: ""
                    label = strings[MessageID.from]
                    error = props.uiState.valuesError
                    enabled = props.uiState.fieldsEnabled
                    inputProps = {
                        it.inputMode = InputMode.numeric
                    }
                    onChange = {
                        props.onReportFilterChanged(
                            props.uiState.reportFilter?.shallowCopy {
                                reportFilterValueBetweenX = it
                            })
                    }
                }

                UstadTextEditField {
                    value = props.uiState.reportFilter?.reportFilterValueBetweenY ?: ""
                    label = strings[MessageID.toC]
                    error = props.uiState.valuesError
                    enabled = props.uiState.fieldsEnabled
                    inputProps = {
                        it.inputMode = InputMode.numeric
                    }
                    onChange = {
                        props.onReportFilterChanged(
                            props.uiState.reportFilter?.shallowCopy {
                                reportFilterValueBetweenY = it
                            })
                    }
                }
            }

            List{
                props.uiState.uidAndLabelList.forEach { uidAndLabel ->
                    ListItem{
                        ListItemText {
                            primary = ReactNode(uidAndLabel.labelName ?: "")
                            onClick = {
                                props.onClickEditFilter(uidAndLabel)
                            }
                        }
                        secondaryAction = IconButton.create {
                            Delete {}
                            onClick = {
                                props.onClickRemoveFilter(uidAndLabel)
                            }
                        }
                    }
                }
            }

            ListItem {

                onClick = {
                    props.onClickNewItemFilter()
                }

                ListItemIcon {
                    + Add.create()
                }

                ListItemText {
                    primary = ReactNode(props.uiState.createNewFilter)
                }
            }
        }
    }
}