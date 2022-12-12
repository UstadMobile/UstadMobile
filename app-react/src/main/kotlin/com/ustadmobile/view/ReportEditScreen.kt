package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.*
import com.ustadmobile.core.viewmodel.ReportEditUiState
import com.ustadmobile.core.viewmodel.ReportSeriesUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.lib.db.entities.ext.shallowCopyReportWithSeriesWithFilters
import com.ustadmobile.mui.components.UstadMessageIdDropDownField
import com.ustadmobile.mui.components.UstadTextEditField
import csstype.px
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
                        fieldsEnabled = props.uiState.fieldsEnabled
                        reportSeriesUiState = props.uiState.reportSeriesUiState
                        selectedReportSeries = reportSeries
                        onClickRemoveSeries = props.onClickRemoveSeries
                        onClickNewFilter = props.onClickNewFilter
                        onClickDeleteReportFilter = props.onClickDeleteReportFilter
                        onReportSeriesChanged = { changedReportSeries ->
                            val index = reportSeriesMutableList.indexOf(reportSeries)
                            reportSeriesMutableList.toMutableList()[index] = changedReportSeries
                            props.onReportSeriesListChanged(reportSeriesMutableList)
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

external interface ReportSeriesListItemProps : Props {

    var fieldsEnabled: Boolean

    var reportSeriesUiState: ReportSeriesUiState

    var onClickRemoveSeries: (ReportSeries) -> Unit

    var onClickNewFilter: (ReportSeries) -> Unit

    var onClickDeleteReportFilter: (ReportFilterWithDisplayDetails) -> Unit

    var selectedReportSeries: ReportSeries

    var onReportSeriesChanged: (ReportSeries) -> Unit

}

private val ReportSeriesListItem = FC<ReportSeriesListItemProps> { props ->

    val strings = useStringsXml()

    var seriesName by useState<String>(props.selectedReportSeries.reportSeriesName ?: "")
    var seriesYAxis by useState<Int>(props.selectedReportSeries.reportSeriesYAxis)
    var seriesSubGroup by useState<Int>(props.selectedReportSeries.reportSeriesSubGroup)

    ListItem {
        children = UstadTextEditField.create {
            value = seriesName
            label = strings[MessageID.title]
            enabled = props.fieldsEnabled
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
        UstadMessageIdDropDownField {
            value = seriesYAxis
            label = strings[MessageID.xapi_options_visual_type]
            options = VisualTypeConstants.VISUAL_TYPE_MESSAGE_IDS
            enabled = props.fieldsEnabled
            onChange = {
                seriesYAxis = it?.value ?: 0
                props.onReportSeriesChanged(
                    props.selectedReportSeries.shallowCopy {
                        reportSeriesYAxis = it?.value ?: 0
                    }
                )
            }
        }
    }

    ListItem {
        UstadMessageIdDropDownField {
            value = seriesSubGroup
            label = strings[MessageID.xapi_options_subgroup]
            options = SubgroupConstants.SUB_GROUP_MESSAGE_IDS
            enabled = props.fieldsEnabled
            onChange = {
                seriesSubGroup = it?.value ?: 0
                props.onReportSeriesChanged(
                    props.selectedReportSeries.shallowCopy {
                        reportSeriesSubGroup = it?.value ?: 0
                    }
                )
            }
        }
    }

    ListItem {
        ListItemText {
            primary = ReactNode(strings[MessageID.filter])
        }
    }

    List {
        props.reportSeriesUiState.filterList.forEach { filter ->
            ListItem {
                ListItemText {
                    primary = ReactNode(filter.person?.fullName() ?: "")
                }
                secondaryAction = IconButton.create {
                    onClick = { props.onClickDeleteReportFilter(filter) }
                    Delete {}
                }
            }
        }
    }

    ListItem {
        onClick = {
            props.onClickNewFilter(props.selectedReportSeries)
        }
        ListItemIcon {
            +Add.create()
        }
        ListItemText {
            primary = ReactNode(strings[MessageID.filter])
        }
    }

    ListItem {
        children = Divider.create {
            orientation = Orientation.horizontal
        }
    }
}

