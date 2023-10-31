package com.ustadmobile.view.contententry.list

import com.ustadmobile.core.MR
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.ContentEntryGetMetaDataFromUriUseCaseJs
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.hooks.ustadViewName
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.impl.nav.NavResult
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListUiState
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryStatementScoreProgress
import com.ustadmobile.lib.db.entities.ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
import com.ustadmobile.mui.components.NavResultReturnerContext
import com.ustadmobile.mui.components.UstadListFilterChipsHeader
import com.ustadmobile.view.contententry.UstadContentEntryListItem
import com.ustadmobile.view.components.UstadFab
import com.ustadmobile.view.components.virtuallist.VirtualList
import com.ustadmobile.view.components.virtuallist.VirtualListOutlet
import com.ustadmobile.view.components.virtuallist.virtualListContent
import emotion.react.css
import web.cssom.*
import js.core.jso
import mui.material.*
import mui.material.List
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.html.ReactHTML.input
import react.router.useLocation
import react.useRef
import react.useRequiredContext
import react.useState
import web.html.HTMLInputElement
import web.html.InputType
import web.url.URL
import mui.icons.material.Folder as FolderIcon
import mui.icons.material.Link as LinkIcon
import mui.icons.material.FileUpload as FileUploadIcon

external interface ContentEntryListScreenProps : Props {

    var uiState: ContentEntryListUiState

    var onClickContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit

    var onClickDownloadContentEntry: (
        ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?) -> Unit

    var onClickImportFromFile: (() -> Unit)?

    var onClickImportFromLink: (() -> Unit)?

    var onClickFilterChip: (MessageIdOption2) -> Unit

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
    val strings = useStringProvider()

    val viewModel = useUstadViewModel { di, savedStateHandle ->
        ContentEntryListViewModel(di, savedStateHandle, location.ustadViewName)
    }

    var addDialogVisible by useState { false }

    val uiStateVal by viewModel.uiState.collectAsState(ContentEntryListUiState())
    val appState by viewModel.appUiState.collectAsState(AppUiState())
    val fileInputRef = useRef<HTMLInputElement>(null)
    val navResultReturner = useRequiredContext(NavResultReturnerContext)


    input {
        ref = fileInputRef
        type = InputType.file
        id = "content_input_file"
        css {
            display = "none".unsafeCast<Display>()
        }
        onChange = {
            it.target.files?.item(0)?.also { file ->
                navResultReturner.sendResult(NavResult(
                    key = ContentEntryGetMetaDataFromUriUseCaseJs.RESULT_KEY_FILE,
                    timestamp = systemTimeInMillis(),
                    result = file,
                ))
                viewModel.onImportFile(URL.createObjectURL(file))
            }
        }
    }

    ContentEntryListScreenComponent {
        uiState = uiStateVal
        onClickContentEntry = viewModel::onClickEntry
        onClickImportFromLink = viewModel::onClickImportFromLink
        onClickImportFromFile = {
            fileInputRef.current?.click()
        }
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
            ListItem {
                ListItemButton {
                    onClick = {
                        viewModel.onClickNewFolder()
                    }

                    ListItemIcon {
                        FolderIcon()
                    }

                    ListItemText {
                        primary = ReactNode(strings[MR.strings.content_editor_create_new_category])
                    }
                }
            }

            ListItem {
                ListItemButton {
                    onClick = {
                        viewModel.onClickImportFromLink()
                    }

                    ListItemIcon {
                        LinkIcon()
                    }

                    ListItemText {
                        primary = ReactNode(strings[MR.strings.content_from_link])
                    }
                }
            }

            ListItem {
                ListItemButton {
                    onClick = {
                        fileInputRef.current?.click()
                    }

                    ListItemIcon {
                        FileUploadIcon()
                    }

                    ListItemText {
                        primary = ReactNode(strings[MR.strings.content_from_file])
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

    val strings = useStringProvider()

    VirtualList {
        style = jso {
            height = "calc(100vh - ${muiAppState.appBarHeight}px)".unsafeCast<Height>()
            width = 100.pct
            contain = Contain.strict
            overflowY = Overflow.scroll
        }

        content = virtualListContent {
            if(props.uiState.showChips) {
                item(key = "filter_chips") {
                    UstadListFilterChipsHeader.create {
                        filterOptions = props.uiState.filterOptions
                        selectedChipId = props.uiState.selectedChipId
                        onClickFilterChip = props.onClickFilterChip
                    }
                }
            }


            if(props.uiState.importFromFileItemVisible) {
                item(key = "import_from_file") {
                    ListItem.create {
                        ListItemButton {
                            onClick = {
                                props.onClickImportFromFile?.also { it.invoke() }
                            }

                            ListItemIcon {
                                FileUploadIcon()
                            }

                            ListItemText {
                                primary = ReactNode(strings[MR.strings.import_from_file])
                            }
                        }
                    }
                }
            }

            if(props.uiState.importFromLinkItemVisible) {
                item(key = "import_from_link") {
                    ListItem.create {
                        ListItemButton {
                            onClick = {
                                props.onClickImportFromLink?.also { it.invoke() }
                            }

                            ListItemIcon {
                                LinkIcon()
                            }

                            ListItemText {
                                primary = ReactNode(strings[MR.strings.import_from_link])
                            }
                        }
                    }
                }
            }

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
