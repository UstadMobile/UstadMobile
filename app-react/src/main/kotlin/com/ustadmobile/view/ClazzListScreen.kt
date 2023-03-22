package com.ustadmobile.view

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.locale.entityconstants.RoleConstants
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.ClazzListUiState
import com.ustadmobile.door.paging.LoadResult
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.mui.common.Sizes
import com.ustadmobile.mui.common.justifyContent
import com.ustadmobile.mui.components.UstadListFilterChipsHeader
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.util.colorForAttendanceStatus
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import csstype.*
import js.core.jso
import mui.icons.material.LensRounded
import mui.icons.material.People
import mui.material.*
import mui.system.responsive
import mui.system.sx
import react.*
import web.dom.css.getComputedStyle
import web.events.Event
import web.events.EventHandler
import web.html.HTMLElement
import web.window.RESIZE
import web.window.WindowEventHandlers
import web.window.window
import kotlin.math.round

external interface ClazzListScreenProps : UstadScreenProps {

    var uiState: ClazzListUiState

    var onClickClazz: (Clazz?) -> Unit

    var onClickSort: (SortOrderOption) -> Unit

    var onClickFilterChip: (MessageIdOption2?) -> Unit

}

private val demoList = (0..100).map { index ->
    ClazzWithListDisplayDetails().apply {
        clazzUid = index.toLong()
        clazzName = "Class $index"
        clazzDesc = "Class Description $index"
        attendanceAverage = 0.3F
        numTeachers = 3
        numStudents = 2
    }
}

val ClazzListScreenPreview = FC<UstadScreenProps> { props ->

    ClazzListScreenComponent2 {
        + props
        uiState = ClazzListUiState(
            clazzList = { ListPagingSource(demoList) }
        )
    }
}

private val ClazzListScreenComponent2 = FC<ClazzListScreenProps> { props ->

    val infiniteQueryResult = usePagingSource(
        props.uiState.clazzList, true, 50
    )

    val containerRef = useRef<HTMLElement>(null)
    val containerDefaultPadding = 48
    //default container max width = 1200 pixels minus 48 px padding
    var containerWidth: Int by useState {
        kotlin.math.min(
            window.innerWidth - Sizes.Sidebar.WidthInPx - 48,
            1200 - containerDefaultPadding
        )
    }
    val cardMinWidth = 320

    useEffect(containerRef.current?.clientWidth) {
        fun calcContainerWidth() {
            val currentEl = containerRef.current
            if(currentEl != null) {
                val computedStyle = getComputedStyle(currentEl)
                containerWidth = currentEl.clientWidth -
                    computedStyle.paddingLeft.filter { it.isDigit() || it == '.' }.toInt() -
                    computedStyle.paddingRight.filter { it.isDigit() }.toInt()
            }
        }

        val eventListener :  EventHandler<Event> = {
            calcContainerWidth()
        }

        window.addEventListener(Event.Companion.RESIZE, eventListener)

        cleanup {
            window.removeEventListener(Event.Companion.RESIZE, eventListener)
        }
    }

    val cardsPerRow = kotlin.math.max(containerWidth / cardMinWidth, 1)

    val cardWidth = containerWidth / cardsPerRow

    VirtualList {
        style = jso {
            height = "calc(100vh - ${props.muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            item {
                UstadListSortHeader.create {
                    activeSortOrderOption = props.uiState.activeSortOrderOption
                    enabled = props.uiState.fieldsEnabled
                    onClickSort = props.onClickSort
                    sortOptions = ClazzListUiState.DEFAULT_SORT_OTIONS
                }
            }

            item {
                UstadListFilterChipsHeader.create {
                    filterOptions = props.uiState.filterOptions
                    selectedChipId = props.uiState.selectedChipId
                    enabled = props.uiState.fieldsEnabled
                    onClickFilterChip = props.onClickFilterChip
                }
            }

            infiniteQueryItemsIndexed(
                infiniteQueryResult = infiniteQueryResult,
                itemToKey = { item, index ->
                    index.toString()
                },
                dataPagesToItems = { pages ->
                    pages.mapNotNull { it as? LoadResult.Page<Int, ClazzWithListDisplayDetails> }.flatMap {
                        it.data
                    }.chunked(cardsPerRow)
                },
            ) { rowClazzes, _ ->

                Stack.create {
                    direction = responsive(StackDirection.row)
                    rowClazzes?.forEach { clazz ->
                        ClazzListItem {
                            width = cardWidth
                            clazzItem = clazz
                            onClickClazz = props.onClickClazz
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



external interface ClazzListItemProps : Props {

    var clazzItem: ClazzWithListDisplayDetails?

    var onClickClazz: (ClazzWithListDisplayDetails) -> Unit

    var width: Int

}

private val ClazzListItem = FC<ClazzListItemProps> { props ->

    val strings = useStringsXml()
    val role = (RoleConstants.ROLE_MESSAGE_IDS.find {
        it.value == props.clazzItem?.clazzActiveEnrolment?.clazzEnrolmentRole
    }?.messageId ?: MessageID.student)

    Card {
        key = props.clazzItem?.clazzUid?.toString()
        onClick = {
            props.clazzItem?.also { props.onClickClazz(it) }
        }
        sx {
            margin = 10.px
            width = (props.width - 20).px
            display = Display.flex
        }

        CardActionArea {
            sx {
                padding = 15.px
            }

            Stack {
                direction = responsive(StackDirection.column)

                Stack {
                    direction = responsive(StackDirection.row)
                    justifyContent = JustifyContent.spaceBetween

                    Stack {
                        direction = responsive(StackDirection.column)

                        Typography {
                            + (props.clazzItem?.clazzName ?: "")
                        }

                        Typography {
                            + (props.clazzItem?.clazzDesc ?: "")
                        }
                    }

                    Stack {
                        direction = responsive(StackDirection.row)

                        + mui.icons.material.Badge.create()

                        + strings[role]
                    }

                }

                Stack {
                    direction = responsive(StackDirection.row)

                    LensRounded {
                        color = colorForAttendanceStatus(
                            props.clazzItem?.attendanceAverage ?: 0.toFloat()
                        )
                        sx {
                            width = 15.px
                            height = 15.px

                            // To align with List Item Button
                            padding = Padding(
                                top = 5.px,
                                bottom = 0.px,
                                right = 0.px,
                                left = 0.px,
                            )
                        }
                    }

                    Typography {
                        + strings[MessageID.x_percent_attended].replace("%1.0f%",
                            round((props.clazzItem?.attendanceAverage ?: 0.toFloat()) * 100).toString())
                    }

                    Box{
                        sx { width = 10.px }
                    }

                    + People.create()

                    Typography {
                        + strings[MessageID.x_teachers_y_students]
                            .replace("%1\$d", props.clazzItem?.numTeachers.toString())
                            .replace("%2\$d", props.clazzItem?.numStudents.toString())
                    }

                }
            }
        }




    }
}