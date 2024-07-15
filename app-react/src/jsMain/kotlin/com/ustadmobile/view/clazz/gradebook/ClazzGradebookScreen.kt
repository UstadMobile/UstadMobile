package com.ustadmobile.view.clazz.gradebook

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.viewmodel.clazz.gradebook.ClazzGradebookUiState
import com.ustadmobile.core.viewmodel.clazz.gradebook.ClazzGradebookViewModel
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useTabAndAppBarHeight
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadBlockIcon
import com.ustadmobile.view.components.UstadPersonAvatar
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import js.objects.jso
import kotlinx.coroutines.flow.Flow
import mui.material.Stack
import mui.material.Box
import mui.material.ListItem
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.StackDirection
import mui.material.Typography
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useRequiredContext
import web.cssom.ClassName
import web.cssom.Color
import web.cssom.Contain
import web.cssom.Display
import web.cssom.GeometryPosition
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.Position
import web.cssom.TextAlign
import web.cssom.TextOverflow
import web.cssom.TransformOrigin
import web.cssom.deg
import web.cssom.integer
import web.cssom.pct
import web.cssom.px
import web.cssom.rotate
import com.ustadmobile.core.viewmodel.clazz.gradebook.thumbnailUri
import com.ustadmobile.util.ext.useLineClamp
import emotion.react.css
import react.dom.html.ReactHTML.div

external interface ClazzGradebookProps: Props {
    var uiState: ClazzGradebookUiState

    var refreshCommandFlow: Flow<RefreshCommand>
}

private val NAME_WIDTH = 240

private val COLUMN_WIDTH = 56

private val COLUMN_HEIGHT = 56

/**
 * See https://stackoverflow.com/questions/15806925/how-to-rotate-text-left-90-degree-and-cell-size-is-adjusted-according-to-text-in
 */
val ClazzGradebookComponent = FC<ClazzGradebookProps> { props ->
    val mediatorResult = useDoorRemoteMediator(props.uiState.results, props.refreshCommandFlow)

    val infiniteQueryResult = usePagingSource(
        mediatorResult.pagingSourceFactory, false, 50
    )

    val theme by useRequiredContext(ThemeContext)

    val tabAndAppBarHeight = useTabAndAppBarHeight()

    val headerRowHeight =  180

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
                    ListItem {
                        sx {
                            width = NAME_WIDTH.px
                            overflowInline = Overflow.clip
                            textOverflow = TextOverflow.ellipsis
                            position = Position.sticky
                            backgroundColor = Color(theme.palette.background.default)

                            //Setting left to a non-auto value, and then leaving top/bottom alone
                            // makes sticky apply to the x-axis, not the y axis, as we want.
                            left = 0.px
                        }

                        ListItemIcon {
                            UstadPersonAvatar {
                                pictureUri = item?.student?.personPicture?.personPictureThumbnailUri
                                personName = item?.student?.person?.fullName()
                            }
                        }

                        ListItemText {
                            primary = div.create {
                                css {
                                    useLineClamp(2)
                                }

                                + (item?.student?.person?.fullName() ?: "null")
                            }
                            primaryTypographyProps = jso {
                                component = div
                            }
                        }
                    }

                    //Progress blocks for each here.
                    props.uiState.courseBlocks.forEach { blockVal ->
                        ClazzGradebookCell {
                            blockStatus = item?.blockStatuses?.firstOrNull {
                                it.sCbUid == blockVal.block?.cbUid
                            }
                            block = blockVal.block
                            width = COLUMN_WIDTH
                            height = COLUMN_HEIGHT
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
                    Box {
                        sx {
                            width = COLUMN_WIDTH.px
                            height = headerRowHeight.px
                            position = Position.relative
                        }

                        Stack {
                            direction = responsive(StackDirection.row)

                            sx {
                                transform = rotate((-90f).deg)
                                transformOrigin = TransformOrigin(
                                    GeometryPosition.left, GeometryPosition.bottom
                                )
                                bottom = 4.px
                                left = 100.pct
                                textOverflow = TextOverflow.ellipsis
                                overflowInline = Overflow.clip
                                position = Position.absolute
                                width = headerRowHeight.px
                                height = COLUMN_WIDTH.px
                            }

                            ListItem {
                                ListItemIcon {
                                    UstadBlockIcon {
                                        title = block.block?.cbTitle ?: ""
                                        courseBlock = block.block
                                        contentEntry = block.contentEntry
                                        pictureUri = block.thumbnailUri
                                    }
                                }

                                ListItemText {
                                    primary = div.create {
                                        css {
                                            useLineClamp(2)
                                        }
                                        + block.block?.cbTitle ?: ""
                                    }
                                    primaryTypographyProps = jso {
                                        component = div
                                    }
                                }
                            }
                        }
                    }
                }
            }

            VirtualListOutlet()
        }
    }
}

val ClazzGradebookScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ClazzGradebookViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(ClazzGradebookUiState())

    ClazzGradebookComponent {
        uiState = uiStateVal
        refreshCommandFlow = viewModel.refreshCommandFlow
    }
}
