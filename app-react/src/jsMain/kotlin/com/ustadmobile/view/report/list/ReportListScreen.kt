package com.ustadmobile.view.report.list

import app.cash.paging.PagingSourceLoadResult
import app.cash.paging.PagingSourceLoadResultPage
import com.ustadmobile.core.domain.report.model.GraphSeries
import com.ustadmobile.core.domain.report.model.ReportOptions2
import com.ustadmobile.core.domain.report.model.ReportResultQueryRow
import com.ustadmobile.core.domain.report.model.ReportSeriesVisualType
import com.ustadmobile.core.domain.report.model.SeriesType
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.hooks.ustadViewName
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.report.list.ReportDataResult
import com.ustadmobile.core.viewmodel.report.list.ReportListUiState
import com.ustadmobile.core.viewmodel.report.list.ReportListViewModel
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.Report
import com.ustadmobile.mui.common.Sizes
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import com.ustadmobile.view.report.graph.ReportGraph
import js.objects.jso
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import mui.material.Card
import mui.material.CardActionArea
import mui.material.CardHeader
import mui.material.Container
import mui.material.Grid
import mui.material.IconButton
import mui.material.Stack
import mui.material.StackDirection
import mui.system.responsive
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.router.useLocation
import react.useMemo
import react.useRef
import react.useState
import tanstack.react.query.UseInfiniteQueryResult
import web.cssom.Color
import web.cssom.Contain
import web.cssom.FontWeight
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import web.cssom.px
import web.html.HTMLElement
import web.window.window

external interface ReportListProps : Props {
    var uiState: ReportListUiState
    var refreshCommandFlow: Flow<RefreshCommand>?
    var onListItemClick: (Report) -> Unit
    var onClickAddItem: () -> Unit
    var onRemoveReport: (Long) -> Unit
    var runReport: (Report) -> Flow<ReportDataResult>
}

external interface ReportListItemProps : Props {
    var report: Report
    var onListItemClick: (Report) -> Unit
    var onRemoveReport: (Long) -> Unit
    var runReport: (Report) -> Flow<ReportDataResult>
}

val ReportListItem = FC<ReportListItemProps> { props ->
    val string = useStringProvider()
    val reportDataFlow = useMemo(props.report.reportUid) {
        props.runReport(props.report)
    }
    val reportDataResult by reportDataFlow.collectAsState(
        ReportDataResult(null, emptyList())
    )

    val graphSeriesList = useMemo(reportDataResult) {
        reportDataResult.options?.series?.mapIndexed { index, reportSeries ->
            GraphSeries(
                type = when (reportSeries.reportSeriesVisualType) {
                    ReportSeriesVisualType.LINE_GRAPH -> SeriesType.LINE
                    else -> SeriesType.BAR
                },
                data = reportDataResult.data.getOrNull(index)?.map {
                    ReportResultQueryRow(
                        xAxis = it.xAxis,
                        yAxis = it.yAxis,
                        subgroup = it.subgroup
                    )
                } ?: emptyList(),
                name = reportSeries.reportSeriesTitle
            )
        } ?: emptyList()
    }

    Card {
        sx = jso {
            margin = 16.px
            padding = 16.px
        }

        CardHeader {
            title = ReactNode(props.report.reportTitle)
            titleTypographyProps = jso {
                sx = jso {
                    fontWeight = FontWeight.normal
                    fontSize = web.cssom.FontSize.medium
                }
            }
            action = IconButton.create {
                mui.icons.material.Close {
                    onClick = { props.onRemoveReport(props.report.reportUid) }
                }
            }
        }

        CardActionArea {
            onClick = { props.onListItemClick(props.report) }

            ReportGraph {
                this.graphSeriesList = graphSeriesList
                this.reportOptions = reportDataResult.options ?: ReportOptions2()
                this.strings = string
                this.compact = true
            }
        }
    }
}

val ReportListComponent2 = FC<ReportListProps> { props ->
    val remoteMediatorResult = useDoorRemoteMediator(
        pagingSourceFactory = props.uiState.reportList,
        refreshCommandFlow = (props.refreshCommandFlow ?: emptyFlow())
    )
    val infiniteQueryResult: UseInfiniteQueryResult<PagingSourceLoadResult<Int, Report>, Throwable> =
        usePagingSource(
            remoteMediatorResult.pagingSourceFactory, true, 50
        )
    val muiAppState = useMuiAppState()
    val containerDefaultPadding = 48
    var containerWidth: Int by useState {
        kotlin.math.min(
            window.innerWidth - Sizes.Sidebar.WidthInPx - 48,
            1200 - containerDefaultPadding
        )
    }
    val containerRef = useRef<HTMLElement>(null)

    val cardMinWidth = 320
    val cardsPerRow = kotlin.math.max(containerWidth / cardMinWidth, 1)

    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }
        content = virtualListContent {
            Grid {
                container = true
                spacing = responsive(2)
                sx = jso {
                    padding = 16.px
                    backgroundColor = Color("#f0f0f0")
                }
                infiniteQueryItemsIndexed(
                    infiniteQueryResult = infiniteQueryResult,
                    itemToKey = { _, index ->
                        index.toString()
                    },
                    dataPagesToItems = { pages ->
                        pages.mapNotNull { it as? PagingSourceLoadResultPage<Int, Report> }
                            .flatMap {
                                it.data
                            }.chunked(cardsPerRow)
                    },
                ) { reports, _ ->
                    Stack.create {
                        direction = responsive(StackDirection.row)
                        reports?.forEach { reportValue ->
                            ReportListItem {
                                this.report = reportValue ?: Report()
                                this.onListItemClick = props.onListItemClick
                                this.onRemoveReport = props.onRemoveReport
                                this.runReport = props.runReport
                            }
                        }
                    }
                }
            }
        }

        Container {
            ref = containerRef

            VirtualListOutlet()
        }
    }

}

val ReportListScreen = FC<Props> {
    val location = useLocation()
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ReportListViewModel(di, savedStateHandle, location.ustadViewName)
    }
    val uiState: ReportListUiState by viewModel.uiState.collectAsState(ReportListUiState())
    val appState by viewModel.appUiState.collectAsState(AppUiState())

    UstadFab {
        fabState = appState.fabState
    }
    ReportListComponent2 {
        this.uiState = uiState
        onListItemClick = viewModel::onClickEntry
        onClickAddItem = viewModel::onClickAdd
        onRemoveReport = viewModel::onRemoveReport
        runReport = viewModel::runReport
    }
}