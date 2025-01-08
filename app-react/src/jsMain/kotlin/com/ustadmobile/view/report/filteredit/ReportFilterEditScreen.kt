package com.ustadmobile.view.report.filteredit

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.Comparisons
import com.ustadmobile.core.domain.report.model.FilterType
import com.ustadmobile.core.domain.report.model.GenderType
import com.ustadmobile.core.domain.report.model.ReportFilter3
import com.ustadmobile.core.domain.report.model.ReportSeriesYAxis
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.report.filteredit.ReportFilterEditUiState
import com.ustadmobile.core.viewmodel.report.filteredit.ReportFilterEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.util.ext.onTextChange
import kotlinx.coroutines.Dispatchers
import mui.material.FormControl
import mui.material.InputLabel
import mui.material.MenuItem
import mui.material.Select
import mui.material.Stack
import mui.material.StackDirection
import mui.material.TextField
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import web.cssom.px

external interface ReportFilterEditScreenProps : Props {
    var uiState: ReportFilterEditUiState
    var onReportFilterChanged: (ReportFilter3?) -> Unit
}

private val ReportFilterEditScreenComponent2 = FC<ReportFilterEditScreenProps> { props ->
    val strings = useStringProvider()

    UstadStandardContainer {
        Stack {
            spacing = responsive(2)
            FormControl {
                fullWidth = true
                InputLabel {
                    id = "Field"
                    shrink = true
                    +ReactNode(strings[MR.strings.y_axis] + "*")
                }

                Select {
                    value = props.uiState.filters?.reportFilterField
                    id = "Field"
                    labelId = "abel"
                    fullWidth = true
                    onChange = { event, _ ->
                        val selectedValue =
                            FilterType.entries.firstOrNull { it.name == event.target.value }
                                ?: FilterType.PERSON_AGE
                        val updatedOptions =
                            props.uiState.filters?.copy(reportFilterField = selectedValue)
                        props.onReportFilterChanged(updatedOptions)
                    }

                    ReportSeriesYAxis.entries.forEach { option ->
                        MenuItem {
                            value = option.label
                            +ReactNode(strings[option.label])
                        }
                    }
                }
            }

            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(10.px)
                FormControl {
                    fullWidth = true
                    InputLabel {
                        id = "Condition"
                        shrink = true
                        +ReactNode(strings[MR.strings.condition] + "*")
                    }

                    Select {
                        value = props.uiState.filters?.reportFilterCondition
                        id = "Condition"
                        labelId = "condition_label"
                        fullWidth = true
                        onChange = { event, _ ->
                            val selectedValue =
                                Comparisons.entries.firstOrNull { it.name == event.target.value }
                                    ?: Comparisons.EQUALS
                            val updatedOptions =
                                props.uiState.filters?.copy(reportFilterCondition = selectedValue)
                            props.onReportFilterChanged(updatedOptions)
                        }

                        Comparisons.entries.forEach { option ->
                            MenuItem {
                                value = option.label
                                +ReactNode(strings[option.label])
                            }
                        }
                    }
                }

                // Value Field - Dropdown or Text Input
                if (props.uiState.filters?.reportFilterField == FilterType.PERSON_GENDER) {
                    FormControl {
                        fullWidth = true
                        InputLabel {
                            id = "Value"
                            shrink = true
                            +ReactNode(strings[MR.strings.value] + "*")
                        }

                        Select {
                            value =
                                GenderType.entries.firstOrNull { it.name == props.uiState.filters?.reportFilterValue }?.label
                                    ?: 0
                            id = "Value"
                            labelId = "abel"
                            fullWidth = true
                            onChange = { event, _ ->
                                val selectedValue =
                                    GenderType.entries.firstOrNull { it.name == event.target.value }
                                        ?: GenderType.FEMALE
                                val updatedOptions =
                                    props.uiState.filters?.copy(reportFilterValue = selectedValue.name)
                                props.onReportFilterChanged(updatedOptions)
                            }

                            GenderType.entries.forEach { option ->
                                MenuItem {
                                    value = option.label
                                    +ReactNode(strings[option.label])
                                }
                            }
                        }
                    }
                } else {
                    TextField {
                        id = "Value"
                        value = props.uiState.filters?.reportFilterValue ?: ""
                        label = ReactNode(strings[MR.strings.value] + "*")
                        onTextChange = { newValue ->
                            val updatedOptions =
                                props.uiState.filters?.copy(reportFilterValue = newValue)
                            props.onReportFilterChanged(updatedOptions)
                        }
                        fullWidth = true
                    }
                }
            }
        }
    }
}

val ReportFilterEditScreenComponent = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ReportFilterEditViewModel(di, savedStateHandle)
    }

    val uiStateVar by viewModel.uiState.collectAsState(
        ReportFilterEditUiState(), Dispatchers.Main.immediate
    )

    ReportFilterEditScreenComponent2 {
        uiState = uiStateVar
        onReportFilterChanged = viewModel::onEntityChanged
    }
}
