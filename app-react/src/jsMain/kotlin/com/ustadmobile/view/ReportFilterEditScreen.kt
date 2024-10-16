package com.ustadmobile.view

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.entityconstants.ConditionConstants
import com.ustadmobile.core.impl.locale.entityconstants.ContentCompletionStatusConstants
import com.ustadmobile.core.impl.locale.entityconstants.FieldConstants
import com.ustadmobile.core.viewmodel.ReportFilterEditUiState
import com.ustadmobile.lib.db.entities.ReportFilter
import com.ustadmobile.lib.db.entities.UidAndLabel
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.view.components.UstadMessageIdSelectField
import com.ustadmobile.mui.components.UstadTextEditField
import com.ustadmobile.view.components.UstadBlankIcon
import web.cssom.px
import mui.icons.material.Add
import mui.icons.material.Delete
import mui.material.*
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.aria.ariaLabel
import web.html.InputMode

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

    val strings = useStringProvider()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            UstadMessageIdSelectField {
                value = props.uiState.reportFilter?.reportFilterField ?: 0
                label = strings[MR.strings.report_filter_edit_field]
                options = FieldConstants.FIELD_MESSAGE_IDS
                error = props.uiState.fieldError
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onReportFilterChanged(
                        props.uiState.reportFilter?.shallowCopy {
                            reportFilterField = it.value
                        })
                }
            }

            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(10.px)

                UstadMessageIdSelectField {
                    value = props.uiState.reportFilter?.reportFilterCondition ?: 0
                    label = strings[MR.strings.report_filter_edit_condition]
                    options = ConditionConstants.CONDITION_MESSAGE_IDS
                    error = props.uiState.conditionsError
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onReportFilterChanged(
                            props.uiState.reportFilter?.shallowCopy {
                                reportFilterCondition = it.value
                            })
                    }
                }

                UstadMessageIdSelectField {
                    value = props.uiState.reportFilter?.reportFilterDropDownValue ?: 0
                    label = strings[MR.strings.report_filter_edit_values]
                    options = ContentCompletionStatusConstants.CONTENT_COMPLETION_STATUS_MESSAGE_IDS
                    error = props.uiState.valuesError
                    enabled = props.uiState.fieldsEnabled
                    onChange = {
                        props.onReportFilterChanged(
                            props.uiState.reportFilter?.shallowCopy {
                                reportFilterDropDownValue = it.value
                            })
                    }
                }
            }

            if (props.uiState.reportFilterValueVisible){
                UstadTextEditField {
                    value = props.uiState.reportFilter?.reportFilterValue ?: ""
                    label = strings[MR.strings.report_filter_edit_values]
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
            }

             if (props.uiState.reportFilterBetweenValueVisible){
                 Stack {
                     direction = responsive(StackDirection.row)
                     spacing = responsive(10.px)

                     UstadTextEditField {
                         value = props.uiState.reportFilter?.reportFilterValueBetweenX ?: ""
                         label = strings[MR.strings.from]
                         error = props.uiState.valuesError
                         enabled = props.uiState.fieldsEnabled
                         fullWidth = true
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
                         label = strings[MR.strings.to_key]
                         error = props.uiState.valuesError
                         enabled = props.uiState.fieldsEnabled
                         fullWidth = true
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
             }



            List {
                ListItem {
                    ListItemButton {
                        onClick = {
                            props.onClickNewItemFilter()
                        }

                        ListItemIcon {
                            Add()
                        }

                        ListItemText {
                            primary = ReactNode(props.uiState.createNewFilter)
                        }
                    }
                }

                if (props.uiState.reportFilterUidAndLabelListVisible){
                    props.uiState.uidAndLabelList.forEach { uidAndLabel ->

                        ListItem{

                            ListItemIcon{
                                + UstadBlankIcon.create()
                            }

                            ListItemText {
                                primary = ReactNode(uidAndLabel.labelName ?: "")
                                onClick = {
                                    props.onClickEditFilter(uidAndLabel)
                                }
                            }
                            secondaryAction = IconButton.create {
                                ariaLabel = strings[MR.strings.delete]
                                Delete {}
                                onClick = {
                                    props.onClickRemoveFilter(uidAndLabel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}