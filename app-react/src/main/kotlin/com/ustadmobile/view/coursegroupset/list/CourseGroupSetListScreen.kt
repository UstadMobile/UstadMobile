package com.ustadmobile.view.coursegroupset.list

import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.util.SortOrderOption
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListUiState
import com.ustadmobile.core.viewmodel.coursegroupset.list.CourseGroupSetListViewModel
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useTabAndAppBarHeight
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.CourseGroupSet
import com.ustadmobile.mui.components.UstadListSortHeader
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import csstype.Contain
import csstype.Height
import csstype.Overflow
import csstype.pct
import js.core.jso
import mui.material.Container
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemText
import mui.material.List
import react.FC
import react.Props
import react.ReactNode
import react.create

external interface CourseGroupSetListComponentProps: Props {

    var uiState: CourseGroupSetListUiState

    var onClickEntry: (CourseGroupSet) -> Unit

    var onChangeSortOption: (SortOrderOption) -> Unit

}

val CourseGroupSetListComponent = FC<CourseGroupSetListComponentProps> { props ->

    val tabAndAppBarHeight = useTabAndAppBarHeight()

    val infiniteQueryResult = usePagingSource(
        pagingSourceFactory = props.uiState.courseGroupSets,
        placeholdersEnabled = true
    )

    VirtualList {
        style = jso {
            height = "calc(100vh - ${tabAndAppBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            item {
                UstadListSortHeader.create {
                    activeSortOrderOption = props.uiState.sortOption
                    enabled = true
                    onClickSort = props.onChangeSortOption
                    sortOptions = props.uiState.sortOptions
                }
            }

            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { it.cgsUid.toString() }
            ) { courseGroupSet ->
                ListItem.create {
                    ListItemButton {
                        onClick = {
                            courseGroupSet?.also(props.onClickEntry)
                        }

                        ListItemText {
                            primary = ReactNode(courseGroupSet?.cgsName ?: "")
                        }
                    }
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
    }

    UstadFab {
        fabState = appState.fabState
    }

}

