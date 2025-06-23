package com.ustadmobile.view.report.detail

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.report.model.GraphSeries
import com.ustadmobile.core.domain.report.model.ReportResultQueryRow
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
import com.ustadmobile.core.domain.report.model.ReportXAxis
import com.ustadmobile.core.domain.report.model.SeriesType
import com.ustadmobile.core.domain.report.utils.ReportFormatter
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailUiState
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailViewModel
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_FEMALE
import com.ustadmobile.lib.db.entities.Person.Companion.GENDER_MALE
import com.ustadmobile.mui.common.xs
import com.ustadmobile.mui.components.UstadStandardContainer
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.report.graph.ReportGraph
import mui.material.Box
import mui.material.Card
import mui.material.Divider
import mui.material.Grid
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.Stack
import mui.system.StackDirection
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.useMemo
import web.cssom.Color
import web.cssom.Overflow
import web.cssom.px

external interface ReportDetailProps : Props {
    var uiState: ReportDetailUiState
}

val ReportDetailScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ReportDetailViewModel(di, savedStateHandle)
    }
    val uiState by viewModel.uiState.collectAsState(ReportDetailUiState())
    val appState by viewModel.appUiState.collectAsState(AppUiState())

    UstadFab { fabState = appState.fabState }
    ReportDetailComponent2 {
        this.uiState = uiState
    }
}

val ReportDetailComponent2 = FC<ReportDetailProps> { props ->
    val string = useStringProvider()

    val graphSeriesList =
        useMemo(listOf(props.uiState.reportResults, props.uiState.reportOptions2.series)) {
            props.uiState.reportOptions2.series.mapIndexed { index, reportSeries ->
                GraphSeries(
                    type = when (reportSeries.reportSeriesVisualType) {
                        ReportSeriesVisualType.LINE_GRAPH -> SeriesType.LINE
                        else -> SeriesType.BAR
                    },
                    data = props.uiState.reportResults.getOrNull(index)?.map { statementRow ->
                        ReportResultQueryRow(
                            xAxis = statementRow.xAxis,
                            yAxis = statementRow.yAxis,
                            subgroup = statementRow.subgroup
                        )
                    } ?: emptyList(),
                    name = reportSeries.reportSeriesTitle
                )
            }
        }
    UstadStandardContainer {
        Stack {
            direction = responsive(StackDirection.column)
            spacing = responsive(8.px)

            ReportGraph {
                this.graphSeriesList = graphSeriesList
                this.reportOptions = props.uiState.reportOptions2
                this.strings = string
            }

            moreOption {
                uiState = props.uiState
            }
        }
    }
}

private val moreOption = FC<ReportDetailProps> { props ->
    val strings = useStringProvider()
    val xAxisType = props.uiState.reportOptions2.xAxis

    UstadStandardContainer {
        props.uiState.reportResults.forEachIndexed { seriesIndex, seriesRows ->
            val seriesConfig = props.uiState.reportOptions2.series.getOrNull(seriesIndex)
            val subgroupNamePart = seriesConfig?.reportSeriesSubGroup?.label?.let { strings[it] }

            val subgroupLabel = if (subgroupNamePart != null) {
                "${strings[MR.strings.subgroup_by]} - $subgroupNamePart"
            } else {
                strings[MR.strings.subgroup_by]
            }

            val headerLabels = listOf(
                strings[MR.strings.x_axis],
                strings[MR.strings.y_axis],
                subgroupLabel
            )

            Card {
                sx {
                    marginBottom = 16.px
                    overflowX = "auto".unsafeCast<Overflow>()
                }

                // Table Header
                Grid {
                    container = true
                    spacing = responsive(2)
                    sx {
                        padding = 8.px
                        backgroundColor = "grey.100".unsafeCast<Color>()
                    }

                    headerLabels.forEach { label ->
                        Grid {
                            item = true
                            xs = 4
                            Typography {
                                +label.toString()
                                variant = TypographyVariant.h6
                            }
                        }
                    }
                }

                Divider {}

                seriesRows.forEach { row ->
                    Grid {
                        container = true
                        spacing = responsive(2)
                        sx {
                            padding = 8.px
                            "&:hover" {
                                backgroundColor = "action.hover".unsafeCast<Color>()
                            }
                        }

                        Grid {
                            item = true
                            xs = 4
                            Typography {
                                +when (xAxisType) {
                                    ReportXAxis.GENDER -> getGenderLabel(row.xAxis, strings)
                                    ReportXAxis.CLASS -> row.xAxis
                                    else -> ReportFormatter.formatDateForReport(
                                        row.xAxis,
                                        xAxisType,
                                    )
                                }
                            }
                        }

                        Grid {
                            item = true
                            xs = 4
                            Typography { +row.yAxis.toString() }
                        }

                        Grid {
                            item = true
                            xs = 4
                            Typography {
                                +when ( seriesConfig?.reportSeriesSubGroup) {
                                    ReportXAxis.GENDER -> getGenderLabel(row.subgroup, strings)
                                    ReportXAxis.CLASS -> row.subgroup
                                    else -> ReportFormatter.formatDateForReport(
                                        row.subgroup,
                                        xAxisType,
                                    )
                                }
                            }
                        }
                    }
                    Divider {}
                }
            }

            Box {
                sx {
                    height = 16.px
                }
            }
        }
    }
}

fun getGenderLabel(rawValue: String, strings: StringProvider): String {
    return when (rawValue) {
        GENDER_FEMALE.toString() ->strings[MR.strings.female]
        GENDER_MALE.toString() -> strings[MR.strings.male]
        else -> rawValue
    }
}