package com.ustadmobile.view.clazz.list

import app.cash.paging.PagingSourceLoadResultPage
import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.hooks.ustadViewName
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.locale.entityconstants.RoleConstants
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListUiState
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel
import com.ustadmobile.hooks.courseTerminologyResource
import com.ustadmobile.hooks.useCourseTerminologyEntries
import com.ustadmobile.hooks.useHtmlToPlainText
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithListDisplayDetails
import com.ustadmobile.mui.common.*
import com.ustadmobile.mui.components.ThemeContext
import com.ustadmobile.mui.components.UstadListFilterChipsHeader
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import web.cssom.*
import js.objects.jso
import mui.icons.material.Add
import mui.icons.material.Login
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.*
import react.router.useLocation
import web.dom.getComputedStyle
import web.events.Event
import web.events.EventHandler
import web.html.HTMLElement
import web.window.window
import mui.icons.material.Badge as BadgeIcon
import com.ustadmobile.hooks.useDateFormatter
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useTimeFormatter
import com.ustadmobile.lib.db.entities.EnrolmentRequest
import com.ustadmobile.mui.components.UstadNothingHereYet
import com.ustadmobile.util.ext.isSettledEmpty
import com.ustadmobile.view.clazz.uriOrDefaultBanner
import com.ustadmobile.view.components.UstadDetailHeader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import web.events.addEventListener
import web.events.removeEventListener
import web.window.Window
import web.window.resize

external interface ClazzListScreenProps : Props {

    var uiState: ClazzListUiState

    var refreshCommandFlow: Flow<RefreshCommand>?

    var onClickClazz: (Clazz) -> Unit

    var onClickSort: (SortOrderOption) -> Unit

    var onClickFilterChip: (MessageIdOption2) -> Unit

    var onClickCancelEnrolmentRequest: (EnrolmentRequest) -> Unit

}

private val ClazzListScreenComponent2 = FC<ClazzListScreenProps> { props ->
    val strings = useStringProvider()

    val remoteMediatorResult = useDoorRemoteMediator(
        props.uiState.clazzList, props.refreshCommandFlow ?: emptyFlow()
    )

    val infiniteQueryResult = usePagingSource(
        remoteMediatorResult.pagingSourceFactory, true, 50
    )

    val isSettledEmpty = infiniteQueryResult.isSettledEmpty(remoteMediatorResult)

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

        val eventListener :  EventHandler<Event, Window> = EventHandler {
            calcContainerWidth()
        }

        window.addEventListener(Event.Companion.resize(), eventListener)

        cleanup {
            window.removeEventListener(Event.Companion.resize(), eventListener)
        }
    }

    val cardsPerRow = kotlin.math.max(containerWidth / cardMinWidth, 1)

    val cardWidth = containerWidth / cardsPerRow

    val muiAppState = useMuiAppState()

    val hasPendingEnrolments = props.uiState.pendingEnrolments.isNotEmpty()

    val timeFormatterVal = useTimeFormatter()
    val dateFormatterVal = useDateFormatter()

    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            if(hasPendingEnrolments) {
                item(key = "pending_enrolment_header") {
                    UstadDetailHeader.create {
                        header = ReactNode(strings[MR.strings.pending_requests])
                    }
                }

                items(
                    list = props.uiState.pendingEnrolments,
                    key = { it.enrolmentRequest?.erUid.toString() }
                ) {
                    PendingEnrolmentListItem.create {
                        request = it
                        timeNow = props.uiState.localDateTimeNow
                        timeFormatter = timeFormatterVal
                        dateFormatter = dateFormatterVal
                        dayOfWeekStrings = props.uiState.dayOfWeekStrings
                        onClickCancel = props.onClickCancelEnrolmentRequest
                    }
                }

                item(key = "pending_enrolment_divider") {
                    Divider.create()
                }
            }

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

            if(isSettledEmpty) {
                item {
                    UstadNothingHereYet.create()
                }
            }

            infiniteQueryItemsIndexed(
                infiniteQueryResult = infiniteQueryResult,
                itemToKey = { _, index ->
                    index.toString()
                },
                dataPagesToItems = { pages ->
                    pages.mapNotNull { it as? PagingSourceLoadResultPage<Int, ClazzWithListDisplayDetails> }.flatMap {
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
    val strings = useStringProvider()
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
        refreshCommandFlow = viewModel.refreshCommandFlow
        onClickClazz = viewModel::onClickEntry
        onClickSort = viewModel::onSortOrderChanged
        onClickFilterChip = viewModel::onClickFilterChip
        onClickCancelEnrolmentRequest = viewModel::onClickCancelEnrolmentRequest
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
                            primary = ReactNode(strings[MR.strings.add_a_new_course])
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
                        primary = ReactNode(strings[MR.strings.join_existing_class])
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

    val theme by  useRequiredContext(ThemeContext)
    val strings = useStringProvider()
    val role = RoleConstants.ROLE_MESSAGE_IDS.find {
        it.value == props.clazzItem?.clazzActiveEnrolment?.clazzEnrolmentRole
    }?.stringResource
    val terminologyEntries = useCourseTerminologyEntries(
        props.clazzItem?.terminology
    )

    val clazzDescription = useHtmlToPlainText(props.clazzItem?.clazzDesc ?: "")

    val badgeContentRef = useRef<HTMLElement>(null)
    var badgeWidth by useState(10)
    useEffect(badgeContentRef.current?.clientWidth) {
        badgeContentRef.current?.also {
            badgeWidth = it.clientWidth
        }
    }

    Box {
        if(role != null) {
            Box {
                ref = badgeContentRef
                sx {
                    position = Position.absolute
                    zIndex = 50_000.unsafeCast<ZIndex>()
                    marginLeft = (props.width - badgeWidth).px
                    backgroundColor = theme.palette.primary.light
                    color = theme.palette.primary.contrastText
                    display = Display.inlineBlock
                    borderRadius = 48.px
                    padding = 4.px
                    whiteSpace = WhiteSpace.nowrap
                    verticalAlign = VerticalAlign.baseline
                }


                Typography {
                    variant = TypographyVariant.caption

                    BadgeIcon {
                        sx {
                            width = 16.px
                            height = 16.px
                        }
                        color = SvgIconColor.inherit
                    }

                    + courseTerminologyResource(terminologyEntries, strings, role)
                }
            }
        }


        Card {
            key = props.clazzItem?.clazzUid?.toString()


            sx {
                width = (props.width - 20).px
                margin = theme.spacing(2)
            }

            CardActionArea {
                sx {
                    verticalAlign = VerticalAlign.top
                }
                CardMedia {
                    sx {
                        height = 96.px
                    }
                    image = props.clazzItem?.coursePicture.uriOrDefaultBanner(
                        props.clazzItem?.clazzName ?: ""
                    )
                }

                onClick = {
                    props.clazzItem?.also { props.onClickClazz(it) }
                }

                CardContent {
                    Typography {
                        variant = TypographyVariant.h5
                        gutterBottom = true
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
                        variant = TypographyVariant.body2
                        color = "text.secondary"
                        + clazzDescription
                    }
                }
            }
        }
    }
}