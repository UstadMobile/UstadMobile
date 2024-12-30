package com.ustadmobile.view.report

import com.ustadmobile.core.domain.report.model.ReportFilter2
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportSeries2
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.locale.entityconstants.ConditionConstants
import com.ustadmobile.core.impl.locale.entityconstants.FieldConstants
import com.ustadmobile.core.impl.locale.entityconstants.ReportSeriesYAxisConstants
import com.ustadmobile.core.impl.locale.entityconstants.ReportXAxisConstants
import com.ustadmobile.core.viewmodel.ReportFilterEditUiState
import com.ustadmobile.core.viewmodel.ReportFilterEditViewModel
import com.ustadmobile.core.viewmodel.person.edit.PersonEditUiState
import com.ustadmobile.core.viewmodel.report.ReportEditUiState
import com.ustadmobile.core.viewmodel.report.ReportEditViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.UstadTextEditField
import com.ustadmobile.view.components.UstadMessageIdSelectField
import kotlinx.coroutines.Dispatchers
import mui.material.Container
import mui.material.Stack
import mui.material.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import web.cssom.px

external interface ReportEditScreenProps : Props {
    var uiState: ReportEditUiState
    var onEntityChanged: (ReportOptions2) -> Unit
    var onSeriesChanged: (ReportSeries2) -> Unit
}

private val ReportEditScreenComponent2 = FC<ReportEditScreenProps> { props ->
    val strings = useStringProvider()

    Container {
        maxWidth = "lg"

        Stack {
            spacing = responsive(16.px)
            direction = responsive(StackDirection.column)

            // Report Title
            UstadTextEditField {
                value = props.uiState.reportOptions2.title ?: ""
                onChange = { newValue ->
                    props.onEntityChanged(props.uiState.reportOptions2.copy(title = newValue))
                }
            }

            // X Axis Selection
//            UstadMessageIdSelectField {
//                value = props.uiState.reportOptions2.xAxis
//                options = ReportXAxisConstants.X_AXIS_OPTIONS
//                onChange = { selectedValue ->
//                    props.onEntityChanged(props.uiState.reportOptions2.copy(xAxis = selectedValue))
//                }
//            }

            // Iterate over Series
            props.uiState.reportOptions2.series.forEach { series ->
                Stack {
                    spacing = responsive(16.px)

                    // Series Title
                    UstadTextEditField {
                        value = series.reportSeriesTitle ?: ""
                        onChange = { newValue ->
                            props.onSeriesChanged(series.copy(reportSeriesTitle = newValue))
                        }
                    }

                    // Y Axis Selection
//                    UstadMessageIdSelectField {
//                        value = series.reportSeriesYAxis?.name
//                        options = ReportSeriesYAxisConstants.Y_AXIS_OPTIONS
//                        onChange = { selectedValue ->
//                            props.onSeriesChanged(series.copy(reportSeriesYAxis = selectedValue?.let { ReportSeries2.YAxis.valueOf(it) }))
//                        }
//                    }
                }
            }
        }
    }
}

val ReportEditScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ReportEditViewModel(di, savedStateHandle)
    }
    val uiStateVar by viewModel.uiState.collectAsState(
        ReportEditUiState(), Dispatchers.Main.immediate)
    ReportEditScreenComponent2 {
        this.uiState = uiStateVar
        onEntityChanged = viewModel::onEntityChanged
        onSeriesChanged = viewModel::onSeriesChanged
    }
}
