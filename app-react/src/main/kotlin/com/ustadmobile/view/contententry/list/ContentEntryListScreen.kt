package com.ustadmobile.view.contententry.list

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringsXml
import com.ustadmobile.core.hooks.ustadViewName
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListUiState
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryStatementScoreProgress
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.view.contententry.UstadContentEntryListItem
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import csstype.*
import js.core.jso
import mui.material.*
import mui.material.List
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.router.useLocation
import react.useState
import mui.icons.material.Folder as FolderIcon

external interface ContentEntryListScreenProps : Props {

    var uiState: ContentEntryListUiState

    var onClickContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit

    var onClickDownloadContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit

}

val ContentEntryListScreenPreview = FC<Props> {

    ContentEntryListScreenComponent {
        uiState = ContentEntryListUiState(
            contentEntryList = {
                ListPagingSource(listOf(
                    ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer().apply {
                        contentEntryUid = 1
                        leaf = false
                        ceInactive = true
                        scoreProgress = ContentEntryStatementScoreProgress().apply {
                            progress = 10
                            penalty = 20
                        }
                        contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                        title = "Content Title 1"
                        description = "Content Description 1"
                    },
                    ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer().apply {
                        contentEntryUid = 2
                        leaf = true
                        ceInactive = false
                        contentTypeFlag = ContentEntry.TYPE_DOCUMENT
                        title = "Content Title 2"
                        description = "Content Description 2"
                    }
                ))
            },
        )
    }
}

val ContentEntryListScreen = FC<Props> {
    val location = useLocation()
    val strings = useStringsXml()

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ContentEntryListViewModel(di, savedStateHandle, location.ustadViewName)
    }

    var addDialogVisible by useState { false }

    val uiStateVal by viewModel.uiState.collectAsState(ContentEntryListUiState())
    val appState by viewModel.appUiState.collectAsState(AppUiState())


    ContentEntryListScreenComponent {
        uiState = uiStateVal
        onClickContentEntry = viewModel::onClickEntry
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
            ListItem {
                ListItemButton {
                    onClick = {
                        viewModel.onClickNewFolder()
                    }

                    ListItemIcon {
                        FolderIcon()
                    }

                    ListItemText {
                        primary = ReactNode(strings[MessageID.content_editor_create_new_category])
                    }
                }
            }
        }
    }
}


private val ContentEntryListScreenComponent = FC<ContentEntryListScreenProps> { props ->

    val infiniteQueryResult = usePagingSource(
        pagingSourceFactory = props.uiState.contentEntryList,
        placeholdersEnabled = true
    )

    val muiAppState = useMuiAppState()

    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            infiniteQueryPagingItems(
                items = infiniteQueryResult,
                key = { it.contentEntryUid.toString() }
            ) { entry ->
                UstadContentEntryListItem.create {
                    onClickContentEntry = props.onClickContentEntry
                    contentEntry = entry
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
