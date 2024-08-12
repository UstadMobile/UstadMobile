package com.ustadmobile.view.courseterminology.list

import com.ustadmobile.core.viewmodel.courseterminology.list.CourseTerminologyListUiState
import com.ustadmobile.core.viewmodel.courseterminology.list.CourseTerminologyListViewModel
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.view.components.UstadBlankIcon
import web.cssom.Height
import web.cssom.pct
import js.objects.jso
import react.FC
import react.Props
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import mui.material.*
import mui.icons.material.Add
import react.ReactNode
import react.create
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.core.MR
import web.cssom.Contain
import web.cssom.Overflow


external interface CourseTerminologyListProps: Props {

    var uiState: CourseTerminologyListUiState

    var onClickAddNewItem: () -> Unit

    var onClickItem: (CourseTerminology) -> Unit

}

val CourseTerminologyListComponent = FC<CourseTerminologyListProps> { props ->

    val infiniteQueryResult = usePagingSource(
        props.uiState.terminologyList, true, 50
    )

    val muiAppState = useMuiAppState()

    val strings = useStringProvider()

    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            if(props.uiState.showAddItemInList) {
                item {
                    ListItem.create {
                        ListItemButton {
                            onClick = {
                                props.onClickAddNewItem()
                            }

                            ListItemIcon {
                                Add()
                            }

                            ListItemText {
                                primary = ReactNode(strings[MR.strings.add_new_terminology])
                            }
                        }
                    }
                }
            }

            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { it.ctUid.toString() }
            ) { terminology ->
                ListItem.create {
                    ListItemButton {
                        onClick = {
                            terminology?.also { props.onClickItem(it) }
                        }

                        ListItemIcon {
                            UstadBlankIcon()
                        }

                        ListItemText {
                            primary = ReactNode(terminology?.ctTitle ?: "")
                        }
                    }
                }
            }
        }

        Container {
            VirtualListOutlet()
        }

    }

}

val CourseTerminologyListScreen = FC<Props> {

    val viewModel = useUstadViewModel {di, savedStateHandle ->
        CourseTerminologyListViewModel(di, savedStateHandle)
    }

    val uiStateVal: CourseTerminologyListUiState by viewModel.uiState.collectAsState(
        CourseTerminologyListUiState()
    )
    val appState by viewModel.appUiState.collectAsState(AppUiState())

    UstadFab {
        fabState = appState.fabState
    }

    CourseTerminologyListComponent {
        uiState = uiStateVal
        onClickAddNewItem = viewModel::onClickAdd
        onClickItem = viewModel::onClickEntry
    }

}
