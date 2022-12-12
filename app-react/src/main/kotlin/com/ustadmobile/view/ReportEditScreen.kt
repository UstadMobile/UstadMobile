package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.*
import com.ustadmobile.core.viewmodel.ReportEditUiState
import com.ustadmobile.core.viewmodel.ReportSeriesUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.lib.db.entities.ext.shallowCopyReportWithSeriesWithFilters
import com.ustadmobile.mui.common.justifyContent
import com.ustadmobile.mui.components.UstadMessageIdDropDownField
import com.ustadmobile.mui.components.UstadTextEditField
import csstype.JustifyContent
import csstype.px
import mui.base.ItemAriaLabelType
import mui.icons.material.Add
import mui.icons.material.Close
import mui.icons.material.Delete
import mui.material.*
import mui.material.Stack
import mui.material.StackDirection
import mui.system.responsive
import react.*

external interface ReportEditScreenProps : Props {

    var uiState: ReportEditUiState

    var onReportChanged: (ReportWithSeriesWithFilters?) -> Unit

    var onReportSeriesListChanged: (List<ReportSeries>?) -> Unit

    var onClickNewSeries: () -> Unit

    var onClickRemoveSeries: (ReportSeries) -> Unit

    var onClickNewFilter: (ReportSeries) -> Unit

    var onClickDeleteReportFilter: (ReportFilterWithDisplayDetails) -> Unit

    var selectedReportSeries: ReportSeries

    var onReportSeriesChanged: (ReportSeries) -> Unit

}

val ReportEditScreenPreview = FC<Props> {
    ReportEditScreenComponent2 {
        uiState = ReportEditUiState(
            reportSeriesUiState = ReportSeriesUiState(
                reportSeriesList = listOf(
                    ReportSeries().apply {
                        reportSeriesUid = 0
                        reportSeriesName = "First Series"
                    },
                    ReportSeries().apply {
                        reportSeriesUid = 1
                        reportSeriesName = "Second Series"
                    },
                    ReportSeries().apply {
                        reportSeriesUid = 2
                        reportSeriesName = "Third Series"
                    }
                ),
                filterList = listOf(
                    ReportFilterWithDisplayDetails().apply {
                        person = Person().apply {
                            firstNames = "John"
                            lastName = "Doe"
                        }
                    },
                    ReportFilterWithDisplayDetails().apply {
                        person = Person().apply {
                            firstNames = "Ahmad"
                            lastName = "Ahmadi"
                        }
                    }
                ),
                deleteButtonVisible = true
            ),
        )
    }
}

private val ReportEditScreenComponent2 = FC<ReportEditScreenProps> { props ->

    val strings = useStringsXml()

    Container {
        maxWidth = "lg"

        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(10.px)

            UstadTextEditField {
                value = props.uiState.report?.reportTitle ?: ""
                label = strings[MessageID.xapi_options_report_title]
                enabled = props.uiState.fieldsEnabled
                error = props.uiState.titleError
                onChange = {
                    props.onReportChanged(
                        props.uiState.report?.shallowCopyReportWithSeriesWithFilters {
                            reportTitle = it
                    })
                }
            }

            UstadTextEditField {
                value = props.uiState.report?.reportDescription ?: ""
                label = strings[MessageID.description]
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onReportChanged(
                        props.uiState.report?.shallowCopyReportWithSeriesWithFilters {
                            reportDescription = it
                    })
                }
            }

            UstadMessageIdDropDownField {
                value = props.uiState.report?.xAxis ?: 0
                label = strings[MessageID.xapi_options_x_axes]
                options = XAxisConstants.X_AXIS_MESSAGE_IDS
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onReportChanged(
                        props.uiState.report?.shallowCopyReportWithSeriesWithFilters {
                            xAxis = it?.value ?: 0
                    })
                }
            }

            UstadMessageIdDropDownField {
                value = props.uiState.report?.reportDateRangeSelection ?: 0
                label = strings[MessageID.time_range]
                options = DateRangeConstants.DATE_RANGE_MESSAGE_IDS
                enabled = props.uiState.fieldsEnabled
                onChange = {
                    props.onReportChanged(
                        props.uiState.report?.shallowCopyReportWithSeriesWithFilters{
                            reportDateRangeSelection = it?.value ?: 0
                    })
                }
            }

            List{
                props.uiState.reportSeriesUiState.reportSeriesList.forEach { reportSeries ->
                    val reportSeriesMutableList = props.uiState.reportSeriesUiState.reportSeriesList
                    ReportSeriesListItem {
                        uiState = props.uiState
                        selectedReportSeries = reportSeries
                        onClickRemoveSeries = props.onClickRemoveSeries
                        onClickNewFilter = props.onClickNewFilter
                        onClickDeleteReportFilter = props.onClickDeleteReportFilter
                        onReportSeriesChanged = { changedReportSeries ->
                            val index = reportSeriesMutableList.indexOf(reportSeries)
                            reportSeriesMutableList.toMutableList()[index] = changedReportSeries
                            onReportSeriesListChanged(reportSeriesMutableList)
                        }
                    }
                }
            }

            ListItem {
                onClick = { props.onClickNewSeries() }
                ListItemText{
                    primary = ReactNode(strings[MessageID.xapi_options_series])
                }
                ListItemIcon {
                    + Add.create()
                }
            }
        }
    }
}

private val ReportSeriesListItem = FC<ReportEditScreenProps> { props ->

    val strings = useStringsXml()

    var seriesName = props.selectedReportSeries.reportSeriesName ?: ""
//    var seriesYAxis = reportSeries.reportSeriesYAxis
//    var seriesSubGroup = reportSeries.reportSeriesSubGroup

    ListItem {
        UstadTextEditField {
            value = seriesName
            label = strings[MessageID.title]
            enabled = props.uiState.fieldsEnabled
            onChange = {
                seriesName = it
                props.onReportSeriesChanged(
                    props.selectedReportSeries.shallowCopy {
                        reportSeriesName = it
                    })
            }
        }
        secondaryAction = IconButton.create(){
            onClick = {
                props.onClickRemoveSeries(props.selectedReportSeries)
            }
            Close { }
        }
    }

    ListItem {

        secondaryAction = IconButton.create(){
            onClick = {
                props.onClickRemoveSeries(props.selectedReportSeries)
            }
            Close { }
        }
    }


//    Container {
//        maxWidth = "lg"
//
//        Stack {
//            direction = responsive(StackDirection.column)
//            spacing = responsive(10.px)
//
//            Stack {
//                direction = responsive(StackDirection.row)
//                justifyContent = JustifyContent.spaceAround
//
//
//
//                Button {
//                    variant = ButtonVariant.text
//                    onClick = {
//                        props.onClickRemoveSeries(props.selectedReportSeries)
//                    }
//
//                    + Close.create()
//                }
//            }
//
//            UstadMessageIdDropDownField {
//                value = props.selectedReportSeries.reportSeriesYAxis
//                label = strings[MessageID.xapi_options_visual_type]
//                options = VisualTypeConstants.VISUAL_TYPE_MESSAGE_IDS
//                enabled = props.uiState.fieldsEnabled
//                onChange = {
//                    props.onReportSeriesChanged(
//                        props.selectedReportSeries.shallowCopy {
//                            reportSeriesYAxis = it?.value ?: 0
//                        }
//                    )
//                }
//            }
//
//            UstadMessageIdDropDownField {
//                value = props.selectedReportSeries.reportSeriesSubGroup
//                label = strings[MessageID.xapi_options_subgroup]
//                options = SubgroupConstants.SUB_GROUP_MESSAGE_IDS
//                enabled = props.uiState.fieldsEnabled
//                onChange = {
//                    props.onReportSeriesChanged(
//                        props.selectedReportSeries.shallowCopy {
//                            reportSeriesSubGroup = it?.value ?: 0
//                        }
//                    )
//                }
//            }
//
//            Typography {
//                +strings[MessageID.filter]
//            }
//
//            List {
//                props.uiState.reportSeriesUiState.filterList.forEach { filter ->
//
//                    ListItem {
//                        ListItemText {
//                            primary = ReactNode(filter.person?.fullName() ?: "")
//                        }
//
//                        secondaryAction = IconButton.create {
//                            onClick = { props.onClickDeleteReportFilter(filter) }
//                            Delete {}
//                        }
//                    }
//                }
//            }
//
//            Button {
//                variant = ButtonVariant.text
//                onClick = {
//                    props.onClickNewFilter(props.selectedReportSeries)
//                }
//
//                +strings[MessageID.filter]
//                +Add.create()
//            }
//
//            Divider()
//        }
//    }
}

