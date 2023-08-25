package com.ustadmobile.view.coursegroupset.list

import com.ustadmobile.core.generated.locale.MessageID
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
import com.ustadmobile.mui.components.UstadAddListItem
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import web.cssom.Contain
import web.cssom.Height
import web.cssom.Overflow
import web.cssom.pct
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

    var onClickAddItem: () -> Unit

}

val CourseGroupSetListComponent = FC<CourseGroupSetListComponentProps> { props ->

    val tabAndAppBarHeight = useTabAndAppBarHeight()
    val strings = useStringsXml()

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
            item(key = "sortheader") {
                UstadListSortHeader.create {
                    activeSortOrderOption = props.uiState.sortOption
                    enabled = true
                    onClickSort = props.onChangeSortOption
                    sortOptions = props.uiState.sortOptions
                }
            }

            props.uiState.individualSubmissionOption?.also { individualOption ->
                item(key = "individual_submission_opt") {
                    CourseGroupSetListItem.create {
                        courseGroupSet = individualOption
                        onClick = props.onClickEntry
                    }
                }
            }

            if(props.uiState.showAddItem) {
                item(key = "new") {
                    UstadAddListItem.create {
                        text = strings[MessageID.add_new_groups]
                        onClickAdd = props.onClickAddItem
                    }
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

