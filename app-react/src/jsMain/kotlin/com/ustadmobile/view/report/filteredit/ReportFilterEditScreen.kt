package com.ustadmobile.view.report.filteredit

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.Comparisons
import com.ustadmobile.core.domain.report.model.FilterType
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

            UstadMessageIdSelectField {
                id = "Field"
                value = props.uiState.filters?.reportFilterField?.value ?: 0
                options = FilterType.entries.map { filterType ->
                    MessageIdOption2(
                        stringResource = filterType.stringResource,
                        value = filterType.value
                    )
                }
                label =strings[MR.strings.field]
                onChange = { selectedValue ->
                    val selectedFilterType =
                        FilterType.entries.firstOrNull { it.value == selectedValue.value }
                            ?: FilterType.PERSON_AGE
                    val updatedOptions =
                        props.uiState.filters?.copy(reportFilterField = selectedFilterType)
                    props.onReportFilterChanged(updatedOptions)
                    println("updatedOptions: $updatedOptions")                     }
            }

            Stack {
                direction = responsive(StackDirection.row)
                spacing = responsive(10.px)

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

                TextField {
                    id = "Value"
                    value = props.uiState.filters?.reportFilterValue ?: ""
                    label = ReactNode(strings[MR.strings.value] + "*")
                    onTextChange = {newValue ->
                        val updatedOptions = props.uiState.filters?.copy(reportFilterValue = newValue)
                        props.onReportFilterChanged(updatedOptions)
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
        ReportFilterEditUiState(), Dispatchers.Main.immediate)

    ReportFilterEditScreenComponent2 {
        uiState = uiStateVar
        onReportFilterChanged = viewModel::onEntityChanged
    }
}
