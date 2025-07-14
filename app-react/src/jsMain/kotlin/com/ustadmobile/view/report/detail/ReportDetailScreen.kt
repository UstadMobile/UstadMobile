package com.ustadmobile.view.report.detail

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.locale.StringResourceUiText
import com.ustadmobile.core.impl.locale.StringUiText
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailUiState
import com.ustadmobile.core.viewmodel.report.detail.ReportDetailViewModel
import com.ustadmobile.hooks.useUstadViewModel
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
    ReportDetailComponent {
        this.uiState = uiState
    }
}

val ReportDetailComponent = FC<ReportDetailProps> { props ->
    val string = useStringProvider()
    props.uiState.reportResult?.let { result ->
        UstadStandardContainer {
            Stack {
                direction = responsive(StackDirection.column)
                spacing = responsive(8.px)

                ReportGraph {
                    this.reportResult = result
                    this.strings = string
                    this.xAxisFormatter = props.uiState.xAxisFormatter
                    this.yAxisFormatter = props.uiState.yAxisFormatter
                }

                moreOption {
                    uiState = props.uiState
                }
            }
        }
    }
}

private val moreOption = FC<ReportDetailProps> { props ->
    val strings = useStringProvider()

    UstadStandardContainer {
        props.uiState.reportResult?.resultSeries?.forEach { series ->
            val subgroupNamePart =
                series.reportSeriesOptions.reportSeriesSubGroup?.label?.let { strings[it] }

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
                                +label
                                variant = TypographyVariant.h6
                            }
                        }
                    }
                }

                Divider {}

                series.data.forEach { row ->
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
                                props.uiState.xAxisFormatter?.format(row.xAxis)?.let { uiText ->
                                    when (uiText) {
                                        is StringUiText -> uiText.text
                                        is StringResourceUiText -> strings.get(uiText.resource)
                                    }
                                } ?: row.xAxis
                            }
                        }

                        Grid {
                            item = true
                            xs = 4
                            Typography {
                                +props.uiState.yAxisFormatter?.format(row.yAxis)?.let { uiText ->
                                    when (uiText) {
                                        is StringUiText -> uiText.text
                                        is StringResourceUiText -> strings.get(uiText.resource)
                                    }
                                }
                            }
                        }

                        Grid {
                            item = true
                            xs = 4
                            Typography {
                                +row.subgroup
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