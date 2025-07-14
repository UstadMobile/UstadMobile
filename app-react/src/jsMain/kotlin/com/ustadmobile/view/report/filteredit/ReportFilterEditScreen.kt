package com.ustadmobile.view.report.filteredit

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.Comparisons
import com.ustadmobile.core.domain.report.model.FilterType
import com.ustadmobile.core.domain.report.model.GenderType
import com.ustadmobile.core.domain.report.model.ReportFilter3
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.viewmodel.report.filteredit.ReportFilterEditUiState
import com.ustadmobile.core.viewmodel.report.filteredit.ReportFilterEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.ThemeContext
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
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.useRequiredContext
import web.cssom.Color
import web.cssom.px
import web.html.InputType

external interface ReportFilterEditScreenProps : Props {
    var uiState: ReportFilterEditUiState
    var onReportFilterChanged: (ReportFilter3?) -> Unit
}

private val ReportFilterEditScreenComponent2 = FC<ReportFilterEditScreenProps> { props ->
    val strings = useStringProvider()
    val theme by useRequiredContext(ThemeContext)

    UstadStandardContainer {
        Stack {
            spacing = responsive(2)
            FormControl {
                fullWidth = true
                InputLabel {
                    id = "field"
                    shrink = true
                    sx {
                        backgroundColor = Color(theme.palette.background.default)
                    }
                    +ReactNode(strings[MR.strings.field] + "*")
                }

                Select {
                    value = props.uiState.filters?.reportFilterField?.name ?: ""
                    id = "field"
                    labelId = "field_label"
                    fullWidth = true
                    onChange = { event, _ ->
                        val selectedValue =
                            FilterType.entries.firstOrNull { it.name == event.target.value }
                                ?: FilterType.PERSON_AGE
                        val updatedOptions =
                            props.uiState.filters?.copy(
                                reportFilterField = selectedValue,
                                reportFilterValue = null,
                                reportFilterCondition = null
                            )
                        props.onReportFilterChanged(updatedOptions)
                    }

                    FilterType.entries.forEach { option ->
                        MenuItem {
                            value = option.name
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
                        id = "condition"
                        shrink = true
                        sx {
                            backgroundColor = Color(theme.palette.background.default)
                        }
                        +ReactNode(strings[MR.strings.condition] + "*")
                    }

                    Select {
                        value = props.uiState.filters?.reportFilterCondition?.name ?: ""
                        id = "condition"
                        labelId = "condition_label"
                        fullWidth = true
                        onChange = { event, _ ->
                            val selectedValue =
                                props.uiState.filterConditionOptions?.comparisonTypes
                                    ?.firstOrNull { it.name == event.target.value }
                                    ?: Comparisons.EQUALS
                            val updatedOptions =
                                props.uiState.filters?.copy(reportFilterCondition = selectedValue)
                            props.onReportFilterChanged(updatedOptions)
                        }

                        props.uiState.filterConditionOptions?.comparisonTypes?.forEach { option ->
                            MenuItem {
                                value = option.name
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
                            id = "value"
                            shrink = true
                            sx {
                                backgroundColor = Color(theme.palette.background.default)
                            }
                            +ReactNode(strings[MR.strings.value] + "*")
                        }

                        Select {
                            value = props.uiState.filters?.reportFilterValue ?: ""
                            id = "value"
                            labelId = "value_label"
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
                                    value = option.name
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
                        type = InputType.number
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