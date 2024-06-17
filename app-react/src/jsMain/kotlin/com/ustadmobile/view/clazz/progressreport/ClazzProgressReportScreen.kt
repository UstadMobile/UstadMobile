package com.ustadmobile.view.clazz.progressreport

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.clazz.progressreport.ClazzProgressReportUiState
import com.ustadmobile.core.viewmodel.clazz.progressreport.ClazzProgressReportViewModel
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useTabAndAppBarHeight
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import js.objects.jso
import kotlinx.coroutines.flow.Flow
import mui.material.Container
import mui.material.Stack
import mui.material.Box
import mui.material.StackDirection
import mui.material.Typography
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.create
import react.useState
import web.cssom.Contain
import web.cssom.Display
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.TextOverflow
import web.cssom.pct
import web.cssom.px
import web.cssom.translatex
import web.html.HTMLElement

external interface ClazzProgressReportProps: Props {
    var uiState: ClazzProgressReportUiState

    var refreshCommandFlow: Flow<RefreshCommand>
}

private val NAME_WIDTH = 240

private val COLUMN_WIDTH = 100

/**
 * To check: try and put the horizontal scrolling into the virtual list element e.g. have one
 * element that handles horizontal AND vertical scrolling
 */
val ClazzProgressReportComponent = FC<ClazzProgressReportProps> { props ->
    val mediatorResult = useDoorRemoteMediator(props.uiState.results, props.refreshCommandFlow)

    val infiniteQueryResult = usePagingSource(
        mediatorResult.pagingSourceFactory, false, 50
    )

    /**
     * It (might) be possible to make the effect smoother by using the event handler itself to
     * directly set the translate property, or, dynamically create/update a css class.
     */
    var scrollX by useState { 0.toDouble() }

    val tabAndAppBarHeight = useTabAndAppBarHeight()

    val headerRowHeight =  50

    val totalWidth = NAME_WIDTH + COLUMN_WIDTH * props.uiState.courseBlocks.size

    Container {
        Box {
            sx {
                width = 100.pct
                overflowX = Overflow.scroll
                overflowY = Overflow.clip
                height = "calc(100vh - ${tabAndAppBarHeight + 8}px)".unsafeCast<Height>()
                contain = Contain.strict
                display = Display.block
            }

            onScroll = { evt ->
                scrollX = evt.target.unsafeCast<HTMLElement>().scrollLeft
            }

            // Course Block header row
            Stack {
                direction = responsive(StackDirection.row)
                sx {
                    height = headerRowHeight.px
                    width = totalWidth.px
                }

                //Spacer
                Box {
                    sx { width = NAME_WIDTH.px }
                }

                props.uiState.courseBlocks.forEach { block ->
                    Typography {
                        sx {
                            width = COLUMN_WIDTH.px
                            //transform = rotate(270.deg)
                            textOverflow = TextOverflow.ellipsis
                            overflowInline = Overflow.clip
                            overflowX = Overflow.clip
                        }

                        + (block.cbTitle ?: "")
                    }
                }
            }

            VirtualList {
                style = jso {
                    height = "calc(100vh - ${tabAndAppBarHeight + headerRowHeight + 8}px)"
                        .unsafeCast<Height>()
                    width = totalWidth.px
                    contain = Contain.strict
                    overflowY = Overflow.scroll
                    display = Display.block
                }

                content = virtualListContent {
                    infiniteQueryPagingItemsIndexed(
                        items = infiniteQueryResult,
                        key = { item, index ->
                            item.student.person?.personUid?.toString() ?: index.toString()
                        }
                    ) { item, index ->
                        Stack.create {
                            direction = responsive(StackDirection.row)

                            //Person name and picture
                            Stack {
                                sx {
                                    transform = translatex(scrollX.px)
                                    width = NAME_WIDTH.px
                                    overflowInline = Overflow.clip
                                    textOverflow = TextOverflow.ellipsis
                                }

                                + (item?.student?.person?.fullName() ?: "null")
                            }

                            //Progress blocks for each here.
                            props.uiState.courseBlocks.forEach { block ->
                                Typography {
                                    sx {
                                        width = COLUMN_WIDTH.px
                                    }

                                    + "-"
                                }
                            }
                        }
                    }
                }

                VirtualListOutlet()
            }


        }
    }
}

val ClazzProgressReportScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ClazzProgressReportViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(ClazzProgressReportUiState())

    ClazzProgressReportComponent {
        uiState = uiStateVal
        refreshCommandFlow = viewModel.refreshCommandFlow
    }
}
