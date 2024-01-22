package com.ustadmobile.view.contententry.list

import com.ustadmobile.core.MR
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.hooks.useStringProvider
import com.ustadmobile.core.hooks.ustadViewName
import com.ustadmobile.core.impl.appstate.AppUiState
import com.ustadmobile.core.paging.ListPagingSource
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListUiState
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.hooks.useMuiAppState
import com.ustadmobile.hooks.usePagingSource
import com.ustadmobile.hooks.useUstadViewModel
import com.ustadmobile.lib.db.entities.ContentEntry
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
import react.useState
import web.html.HTMLInputElement
import web.html.InputType
import web.url.URL
import mui.icons.material.Folder as FolderIcon
import mui.icons.material.Link as LinkIcon
import mui.icons.material.FileUpload as FileUploadIcon

external interface ContentEntryListScreenProps : Props {

    var uiState: ContentEntryListUiState

    var onClickContentEntry: (ContentEntry?) -> Unit

    var onClickImportFromFile: (() -> Unit)?

    var onClickImportFromLink: (() -> Unit)?

    var onClickFilterChip: (MessageIdOption2) -> Unit

}

@Suppress("unused") //For development purposes - will be removed by DCE in production anyway
val ContentEntryListScreenPreview = FC<Props> {

    ContentEntryListScreenComponent {
        uiState = ContentEntryListUiState(
            contentEntryList = {
                ListPagingSource(listOf(
                    ContentEntry().apply {
                        contentEntryUid = 1
                        leaf = false
                        ceInactive = true
                        contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                        title = "Content Title 1"
                        description = "Content Description 1"
                    },
                    ContentEntry().apply {
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

    val uiStateVal by viewModel.uiState.collectAsState(ContentEntryListUiState())
    val appState by viewModel.appUiState.collectAsState(AppUiState())
    val fileInputRef = useRef<HTMLInputElement>(null)

    input {
        ref = fileInputRef
        type = InputType.file
        id = "content_input_file"
        css {
            display = "none".unsafeCast<Display>()
        }
        onChange = {
            it.target.files?.item(0)?.also { file ->
                viewModel.onImportFile(URL.createObjectURL(file), file.name)
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
        fabState = appState.fabState
    }

    Dialog {
        open = uiStateVal.createNewOptionsVisible
        onClose = { _, _ ->
            viewModel.onDismissCreateNewOptions()
        }

        List {
            ListItem {
                ListItemButton {
                    id = "new_content_folder"
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
                    id = "new_content_from_link"
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
                    id = "new_content_from_file"
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
                        id = "content_entry_filter_chip_box"
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
