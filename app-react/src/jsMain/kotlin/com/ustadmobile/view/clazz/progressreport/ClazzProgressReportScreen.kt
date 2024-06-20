package com.ustadmobile.view.clazz.progressreport

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.clazz.progressreport.ClazzProgressReportUiState
import com.ustadmobile.core.viewmodel.clazz.progressreport.ClazzProgressReportViewModel
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useTabAndAppBarHeight
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import js.objects.jso
import kotlinx.coroutines.flow.Flow
import mui.material.Stack
import mui.material.Box
import mui.material.StackDirection
import mui.material.Typography
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.create
import react.useRequiredContext
import web.cssom.ClassName
import web.cssom.Color
import web.cssom.Contain
import web.cssom.Display
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.Position
import web.cssom.TextOverflow
import web.cssom.integer
import web.cssom.pct
import web.cssom.px

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

    val theme by useRequiredContext(ThemeContext)

    val tabAndAppBarHeight = useTabAndAppBarHeight()

    val headerRowHeight =  30

    val totalWidth = NAME_WIDTH + COLUMN_WIDTH * props.uiState.courseBlocks.size

    //account for half of scrollbar height?
    val heightMargin = 4

    VirtualList {
        style = jso {
            height = "calc(100vh - ${tabAndAppBarHeight + heightMargin}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
            overflowX = Overflow.scroll
            display = Display.block
        }

        //Css class scrollbarAlwaysVisible is used in index.css to exempt the element from hiding
        //scrollbars as would normally happen.
        className = ClassName("VirtualList scrollbarAlwaysVisible")
        content = virtualListContent {
            infiniteQueryPagingItemsIndexed(
                items = infiniteQueryResult,
                key = { item, index ->
                    item.student.person?.personUid?.toString() ?: index.toString()
                }
            ) { item, index ->
                Stack.create {
                    direction = responsive(StackDirection.row)
                    sx {
                        width = totalWidth.px
                        zIndex = integer(900)
                    }

                    //Person name and picture
                    Stack {
                        sx {
                            width = NAME_WIDTH.px
                            overflowInline = Overflow.clip
                            textOverflow = TextOverflow.ellipsis
                            paddingTop = 16.px
                            paddingBottom = 16.px
                            position = Position.sticky
                            backgroundColor = Color(theme.palette.background.default)

                            //Setting left to a non-auto value, and then leaving top/bottom alone
                            // makes sticky apply to the x-axis, not the y axis, as we want.
                            left = 0.px
                        }

                        + (item?.student?.person?.fullName() ?: "null")
                    }

                    //Progress blocks for each here.
                    props.uiState.courseBlocks.forEach { block ->
                        Typography {
                            sx {
                                width = COLUMN_WIDTH.px
                                paddingTop = 16.px
                                paddingBottom = 16.px
                            }

                            + "-"
                        }
                    }
                }
            }
        }

        Stack {
            direction = responsive(StackDirection.column)

            // Course Block header row
            Stack {
                direction = responsive(StackDirection.row)
                sx {
                    height = headerRowHeight.px
                    width = totalWidth.px
                    backgroundColor = Color(theme.palette.background.default)

                    //Setting top to a non-auto value, and then leaving left/right alone
                    // makes sticky apply to the y-axis, not the x axis, as we want.
                    position = Position.sticky
                    top = 0.px
                    zIndex = integer(1_000)
                }

                //Spacer
                Box {
                    sx {
                        width = NAME_WIDTH.px
                        height = headerRowHeight.px
                        backgroundColor = Color(theme.palette.background.default)

                        position = Position.sticky
                        left = 0.px
                        top = 0.px
                        zIndex = integer(1_100)
                    }
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

            VirtualListOutlet()
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
