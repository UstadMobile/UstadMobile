package com.ustadmobile.view.report.filteredit

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.Comparisons
import com.ustadmobile.core.domain.report.model.FilterType
import com.ustadmobile.core.domain.report.model.GenderType
import com.ustadmobile.core.domain.report.model.ReportFilter3
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.report.filteredit.ReportFilterEditUiState
import com.ustadmobile.core.viewmodel.report.filteredit.ReportFilterEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.util.ext.onTextChange
import com.ustadmobile.view.components.UstadMessageIdSelectField
import kotlinx.coroutines.Dispatchers
import web.cssom.px
import mui.material.*
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode

external interface ReportFilterEditScreenProps : Props {
    var uiState: ReportFilterEditUiState
    var onReportFilterChanged: (ReportFilter3?) -> Unit
}

private val ReportFilterEditScreenComponent2 = FC<ReportFilterEditScreenProps> { props ->
    val strings = useStringProvider()

    UstadStandardContainer {
        Stack {
            spacing = responsive(2)

            // Filter Type Dropdown
            UstadMessageIdSelectField {
                id = "Field"
                value = props.uiState.filters?.reportFilterField?.value ?: 0
                options = FilterType.entries.map { filterType ->
                    MessageIdOption2(
                        stringResource = filterType.stringResource,
                        value = filterType.value
                    )
                }
                label = strings[MR.strings.field]
                onChange = { selectedValue ->
                    val selectedFilterType =
                        FilterType.entries.firstOrNull { it.value == selectedValue.value }
                            ?: FilterType.PERSON_AGE
                    val updatedOptions =
                        props.uiState.filters?.copy(
                            reportFilterField = selectedFilterType,
                            reportFilterValue = null, // Reset value
                            reportFilterCondition = null // Reset condition
                        )
                    props.onReportFilterChanged(updatedOptions)
                }
            }

            // Condition and Value Fields
            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(10.px)

                // Condition Dropdown
                UstadMessageIdSelectField {
                    id = "Condition"
                    value = props.uiState.filters?.reportFilterCondition?.value ?: 0
                    options = Comparisons.entries.map { comparison ->
                        MessageIdOption2(
                            stringResource = comparison.stringResource,
                            value = comparison.value
                        )
                    }
                    label = strings[MR.strings.condition]
                    onChange = { selectedValue ->
                        val selectedComparison =
                            Comparisons.entries.firstOrNull { it.value == selectedValue.value }
                                ?: Comparisons.EQUALS
                        val updatedOptions =
                            props.uiState.filters?.copy(reportFilterCondition = selectedComparison)
                        props.onReportFilterChanged(updatedOptions)
                    }
                }

                // Value Field - Dropdown or Text Input
                if (props.uiState.filters?.reportFilterField == FilterType.PERSON_GENDER) {
                    UstadMessageIdSelectField {
                        id = "Value"
                        value =
                            GenderType.entries.firstOrNull { it.name == props.uiState.filters?.reportFilterValue }?.value
                                ?: 0
                        options = GenderType.entries.map { gender ->
                            MessageIdOption2(
                                stringResource = gender.stringResource,
                                value = gender.value
                            )
                        }
                        label = strings[MR.strings.value]
                        onChange = { selectedValue ->
                            val selectedGender =
                                GenderType.entries.firstOrNull { it.value == selectedValue.value }
                                    ?: GenderType.OTHER
                            val updatedOptions =
                                props.uiState.filters?.copy(reportFilterValue = selectedGender.name)
                            props.onReportFilterChanged(updatedOptions)
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
