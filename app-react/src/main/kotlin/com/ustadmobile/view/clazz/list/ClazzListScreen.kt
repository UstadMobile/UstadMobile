package com.ustadmobile.view.clazz.list

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.hooks.ustadViewName
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.locale.entityconstants.RoleConstants
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListUiState
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.door.paging.LoadResult
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.mui.common.*
import com.ustadmobile.mui.components.UstadListFilterChipsHeader
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.mui.components.UstadRawHtml
import com.ustadmobile.util.colorForAttendanceStatus
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import csstype.*
import js.core.jso
import mui.icons.material.Add
import mui.icons.material.LensRounded
import mui.icons.material.Login
import mui.icons.material.People
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.*
import react.router.useLocation
import web.dom.css.getComputedStyle
import web.events.Event
import web.events.EventHandler
import web.html.HTMLElement
import web.window.RESIZE
import web.window.window
import kotlin.math.round

external interface ClazzListScreenProps : Props {

    var uiState: ClazzListUiState

    var onClickClazz: (Clazz) -> Unit

    var onClickSort: (SortOrderOption) -> Unit

    var onClickLeavingReason: () -> Unit

    var onClickFilterChip: (MessageIdOption2) -> Unit

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

    val muiAppState = useMuiAppState()

    val strings = useStringsXml()

    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
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
                Button.create {
                    id = "leaving_reason_button"
                    onClick = { props.onClickLeavingReason() }
                    variant = ButtonVariant.contained
                    + strings[MessageID.leaving_reasons].uppercase()
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

val ClazzListScreen = FC<Props> { props ->
    val strings = useStringsXml()
    val location = useLocation()
    var addDialogVisible: Boolean by useState { false }


    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ClazzListViewModel(di, savedStateHandle, location.ustadViewName)
    }

    val uiState: ClazzListUiState by viewModel.uiState.collectAsState(ClazzListUiState())
    val appState by viewModel.appUiState.collectAsState(AppUiState())


    ClazzListScreenComponent2 {
        + props
        this.uiState = uiState
        onClickClazz = viewModel::onClickEntry
        onClickSort = viewModel::onSortOrderChanged
        onClickLeavingReason = viewModel::onClickLeavingReason
        onClickFilterChip = viewModel::onClickFilterChip
    }

    UstadFab {
        fabState = appState.fabState.copy(
            onClick = {
                addDialogVisible = true
            }
        )
    }

    Dialog {
        open = addDialogVisible
        onClose = { _, _ ->
            addDialogVisible = false
        }

        List {
            if(uiState.canAddNewCourse) {
                ListItem {
                    ListItemButton {
                        onClick = {
                            addDialogVisible = false
                            viewModel.onClickAdd()
                        }

                        ListItemIcon {
                            Add { }
                        }

                        ListItemText {
                            primary = ReactNode(strings[MessageID.add_a_new_course])
                        }


                    }
                }
            }

            ListItem {
                ListItemButton {
                    onClick = {
                        addDialogVisible = false
                        viewModel.onClickJoinExistingClazz()
                    }

                    ListItemIcon {
                        Login { }
                    }

                    ListItemText {
                        primary = ReactNode(strings[MessageID.join_existing_class])
                    }


                }
            }

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
    val role = RoleConstants.ROLE_MESSAGE_IDS.find {
        it.value == props.clazzItem?.clazzActiveEnrolment?.clazzEnrolmentRole
    }?.messageId

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
                            variant = TypographyVariant.h6
                            + (props.clazzItem?.clazzName ?: "")
                        }

                        Typography {
                            sx {
                                webKitLineClamp = 2
                                display = DisplayWebkitBox
                                webkitBoxOrient = "vertical"
                                overflow = Overflow.hidden
                                textOverflow = TextOverflow.ellipsis
                            }

                            UstadRawHtml {
                                html = (props.clazzItem?.clazzDesc ?: "")
                            }
                        }
                    }


                    if(role != null) {
                        Stack {
                            direction = responsive(StackDirection.row)

                            + mui.icons.material.Badge.create()

                            + strings[role]
                        }
                    }
                }

                Stack {
                    direction = responsive(StackDirection.row)

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