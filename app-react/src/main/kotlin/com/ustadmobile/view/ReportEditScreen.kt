package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.*
import com.ustadmobile.core.viewmodel.ReportEditUiState
import com.ustadmobile.core.viewmodel.ReportSeriesUiState
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.ReportFilterWithDisplayDetails
import com.ustadmobile.lib.db.entities.ReportSeries
import com.ustadmobile.lib.db.entities.ReportWithSeriesWithFilters
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import com.ustadmobile.lib.db.entities.ext.shallowCopyReportWithSeriesWithFilters
import com.ustadmobile.mui.components.UstadMessageIdDropDownField
import com.ustadmobile.mui.components.UstadTextEditField
import com.ustadmobile.view.components.UstadBlankIcon
import csstype.px
import mui.icons.material.Add
import mui.icons.material.Close
import mui.icons.material.Delete
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface ReportEditScreenProps : Props {

    var uiState: ReportEditUiState

    var onReportChanged: (ReportWithSeriesWithFilters?) -> Unit

    var onChangedReportSeries: (ReportSeries?) -> Unit

    var onClickNewSeries: () -> Unit

    var onClickRemoveSeries: (ReportSeries) -> Unit

    var onClickNewFilter: (ReportSeries) -> Unit

    var onClickDeleteReportFilter: (ReportFilterWithDisplayDetails) -> Unit

    var reportSeries: ReportSeries

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
                props.uiState.reportSeriesUiState.reportSeriesList.forEach { reportSeriesItem ->
                    ReportSeriesListItem {
                        reportSeries = reportSeriesItem
                        uiState = props.uiState
                        onClickRemoveSeries = props.onClickRemoveSeries
                        onClickNewFilter = props.onClickNewFilter
                        onClickDeleteReportFilter = props.onClickDeleteReportFilter
                        onChangedReportSeries = props.onChangedReportSeries
                    }
                }
            }

            ListItem {
                onClick = { props.onClickNewSeries() }
                ListItemIcon {
                    + Add.create()
                }
                ListItemText{
                    primary = ReactNode(strings[MessageID.xapi_options_series])
                }
            }
        }
    }
}

private val ReportSeriesListItem = FC<ReportEditScreenProps> { props ->

    val strings = useStringsXml()

    ListItem {
        children = UstadTextEditField.create {
            value = props.reportSeries.reportSeriesName ?: ""
            label = strings[MessageID.title]
            enabled = props.uiState.fieldsEnabled
            fullWidth = true
            onChange = {
                props.onChangedReportSeries(
                    props.reportSeries.shallowCopy {
                        reportSeriesName = it
                    })
            }
        }

        secondaryAction = IconButton.create(){
            onClick = {
                props.onClickRemoveSeries(props.reportSeries)
            }
            Close { }
        }
    }

    ListItem {
        UstadMessageIdDropDownField {
            value = props.reportSeries.reportSeriesYAxis
            label = strings[MessageID.xapi_options_y_axes]
            options = YAxisConstants.Y_AXIS_MESSAGE_IDS
            enabled = props.uiState.fieldsEnabled
            onChange = {
                props.onChangedReportSeries(
                    props.reportSeries.shallowCopy {
                        reportSeriesYAxis = it?.value ?: 0
                    }
                )
            }
        }
    }

    ListItem {
        UstadMessageIdDropDownField {
            value = props.reportSeries.reportSeriesVisualType
            label = strings[MessageID.xapi_options_visual_type]
            options = VisualTypeConstants.VISUAL_TYPE_MESSAGE_IDS
            enabled = props.uiState.fieldsEnabled
            onChange = {
                props.onChangedReportSeries(
                    props.reportSeries.shallowCopy {
                        reportSeriesVisualType = it?.value ?: 0
                    }
                )
            }
        }
    }

    ListItem {
        UstadMessageIdDropDownField {
            value = props.reportSeries.reportSeriesSubGroup
            label = strings[MessageID.xapi_options_subgroup]
            options = SubgroupConstants.SUB_GROUP_MESSAGE_IDS
            enabled = props.uiState.fieldsEnabled
            onChange = {
                props.onChangedReportSeries(
                    props.reportSeries.shallowCopy {
                        reportSeriesSubGroup = it?.value ?: 0
                    }
                )
            }
        }
    }

    List {

        ListItem {
            ListItemText {
                primary = ReactNode(strings[MessageID.filter])
            }
        }

        ListItem {
            onClick = {
                props.onClickNewFilter(props.reportSeries)
            }
            ListItemIcon {
                +Add.create()
            }
            ListItemText {
                primary = ReactNode(strings[MessageID.filter])
            }
        }

        props.uiState.reportSeriesUiState.filterList.forEach { filter ->
            ListItem {

                ListItemIcon{
                    UstadBlankIcon()
                }

                ListItemText {
                    primary = ReactNode(filter.person?.fullName() ?: "")
                    secondary = Divider.create {
                        orientation = Orientation.horizontal
                        sx {
                            height = 1.px
                        }
                    }
                }

                secondaryAction = IconButton.create {
                    onClick = { props.onClickDeleteReportFilter(filter) }
                    Delete {}
                }
            }
        }
    }

    ListItem {
        divider = true
    }
}