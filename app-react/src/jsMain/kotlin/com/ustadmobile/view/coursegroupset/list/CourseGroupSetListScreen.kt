package com.ustadmobile.view.coursegroupset.list

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListUiState
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListViewModel
import com.ustadmobile.hooks.useDoorRemoteMediator
import com.ustadmobile.hooks.useEmptyFlow
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useTabAndAppBarHeight
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.mui.components.UstadAddListItem
import com.ustadmobile.mui.components.UstadNothingHereYet
import com.ustadmobile.util.ext.isSettledEmpty
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import emotion.react.css
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
import js.objects.jso
import kotlinx.coroutines.flow.Flow
import mui.material.Container
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemText
import mui.material.List
import react.FC
import react.Fragment
import react.Props
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.div
import web.cssom.px

external interface CourseGroupSetListComponentProps: Props {

    var uiState: CourseGroupSetListUiState

    var refreshCommandFlow: Flow<RefreshCommand>?

    var onClickEntry: (CourseGroupSet) -> Unit

    var onChangeSortOption: (SortOrderOption) -> Unit

    var onClickAddItem: () -> Unit

}

val CourseGroupSetListComponent = FC<CourseGroupSetListComponentProps> { props ->

    val tabAndAppBarHeight = useTabAndAppBarHeight()
    val strings = useStringProvider()

    val emptyRefreshCommandFlow = useEmptyFlow<RefreshCommand>()

    val mediatorResult = useDoorRemoteMediator(
        props.uiState.courseGroupSets, props.refreshCommandFlow ?: emptyRefreshCommandFlow
    )

    val infiniteQueryResult = usePagingSource(
        pagingSourceFactory = mediatorResult.pagingSourceFactory,
        placeholdersEnabled = true
    )

    val isSettledEmpty = infiniteQueryResult.isSettledEmpty(mediatorResult)

    VirtualList {
        style = jso {
            height = "calc(100vh - ${tabAndAppBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            item(key = "sortheader") {
                UstadListSortHeader.create {
                    activeSortOrderOption = props.uiState.sortOption
                    enabled = true
                    onClickSort = props.onChangeSortOption
                    sortOptions = props.uiState.sortOptions
                }
            }

            //Using the if statement around the items does not seem to get invalidated as expected
            // This might require reconsideration of how virtualListcontent works. In the meantime
            // always create a fragment, and put the showAdditem within it.
            item(key = "new") {
                Fragment.create {
                    if(props.uiState.showAddItem) {
                        UstadAddListItem {
                            id = "add_new_groups"
                            text = strings[MR.strings.add_new_groups]
                            onClickAdd = props.onClickAddItem
                        }
                    }else {
                        div {
                            css {
                                height = 1.px
                            }
                        }
                    }
                }
            }


            if(isSettledEmpty) {
                item("empty_state") {
                    UstadNothingHereYet.create()
                }
            }

            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { it.cgsUid.toString() }
            ) { courseGroupSetItem ->
                CourseGroupSetListItem.create {
                    courseGroupSet = courseGroupSetItem
                    onClick = props.onClickEntry
                }
            }
        }

        Container {
            List {
                VirtualListOutlet()
            }
        }

    }
}

external interface CourseGroupSetListItemProps : Props {
    var courseGroupSet: CourseGroupSet?
    var onClick: (CourseGroupSet) -> Unit
}

private val CourseGroupSetListItem = FC<CourseGroupSetListItemProps> { props ->
    ListItem {
        ListItemButton {
            onClick = {
                props.courseGroupSet?.also(props.onClick)
            }

            ListItemText {
                inset = true
                primary = ReactNode(props.courseGroupSet?.cgsName ?: "")
            }
        }
    }
}

val CourseGroupSetListScreen = FC<Props> {
    val viewModel = useUstadViewModel { di, savedStateHandle ->
        CourseGroupSetListViewModel(di, savedStateHandle)
    }

    val uiStateVal by viewModel.uiState.collectAsState(CourseGroupSetListUiState())
    val appState by viewModel.appUiState.collectAsState(AppUiState())

    CourseGroupSetListComponent {
        uiState = uiStateVal
        onClickEntry = viewModel::onClickEntry
        onChangeSortOption = viewModel::onSortOptionChanged
        onClickAddItem = viewModel::onClickAdd
    }

    UstadFab {
        fabState = appState.fabState
    }

}

