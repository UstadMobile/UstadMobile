package com.ustadmobile.libuicompose.view.contententry.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListUiState
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.libuicompose.view.contententry.UstadContentEntryListItem
import com.ustadmobile.libuicompose.components.ustadPagedItems
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.libuicompose.components.UstadBottomSheetOption
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.appstate.UstadContextMenuItem
import com.ustadmobile.core.paging.RefreshCommand
import com.ustadmobile.core.util.MessageIdOption2
import com.ustadmobile.lib.db.composites.ContentEntryAndListDetail
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.libuicompose.components.PickFileOptions
import com.ustadmobile.libuicompose.components.PickType
import com.ustadmobile.libuicompose.components.UstadFileDropZone
import com.ustadmobile.libuicompose.components.UstadFilePickResult
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadListFilterChipsHeader
import com.ustadmobile.libuicompose.components.UstadPickFileOpts
import com.ustadmobile.libuicompose.components.rememberUstadFilePickLauncher
import com.ustadmobile.libuicompose.paging.rememberDoorRepositoryPager
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import com.ustadmobile.libuicompose.util.rememberEmptyFlow
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentEntryListScreenForViewModel(
    viewModel: ContentEntryListViewModel
) {
    val uiState by viewModel.uiState.collectAsState(ContentEntryListUiState())

    val filePickLauncher = rememberUstadFilePickLauncher {
        viewModel.onImportFile(fileUri = it.uri, fileName = it.fileName)
    }

    ContentEntryListScreen(
        uiState = uiState,
        onClickContentEntry = viewModel::onClickEntry,
        onFileDropped = {
            viewModel.onImportFile(fileUri = it.uri, fileName = it.fileName)
        },
        onClickFilterChip = viewModel::onClickFilterChip,
        onClickImportFile = {
            filePickLauncher(PickFileOptions(pickType = PickType.FILE))
        },
        onClickImportFromLink = viewModel::onClickImportFromLink,
        onSetSelected = viewModel::onSetSelected,
        onClickSelectThisFolder = viewModel::onClickSelectThisFolder,
        contextMenuItems = viewModel::createContextMenuItemsForEntry,
        refreshCommandFlow = viewModel.refreshCommandFlow,
    )

    if(uiState.createNewOptionsVisible) {
        ModalBottomSheet(
            onDismissRequest = viewModel::onDismissCreateNewOptions
        ) {

            UstadBottomSheetOption(
                modifier = Modifier.clickable {
                    viewModel.onClickNewFolder()
                },
                headlineContent = {
                    Text(stringResource(MR.strings.content_editor_create_new_category))
                },
                leadingContent = {
                    Icon(Icons.Default.Folder, contentDescription = null)
                }
            )

            UstadBottomSheetOption(
                modifier = Modifier.clickable {
                    viewModel.onDismissCreateNewOptions()
                    filePickLauncher(PickFileOptions(pickType = PickType.FILE))
                },
                headlineContent = {
                    Text(stringResource(MR.strings.content_from_file))
                },
                leadingContent = {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                }
            )

            UstadBottomSheetOption(
                modifier = Modifier.clickable {
                    viewModel.onClickImportFromLink()
                },
                headlineContent = {
                    Text(stringResource(MR.strings.content_from_link))
                },
                leadingContent = {
                    Icon(Icons.Default.Link, contentDescription = null)
                }
            )

        }
    }

}

@Composable
fun ContentEntryListScreen(
    uiState: ContentEntryListUiState = ContentEntryListUiState(),
    refreshCommandFlow: Flow<RefreshCommand> = rememberEmptyFlow(),
    onClickContentEntry: (ContentEntry?) -> Unit = { },
    onFileDropped: (UstadFilePickResult) -> Unit = { },
    onClickFilterChip: (MessageIdOption2) -> Unit = { },
    onClickImportFile: () -> Unit = { },
    onClickImportFromLink: () -> Unit = { },
    onSetSelected: (entry: ContentEntryAndListDetail, selected: Boolean) -> Unit = { _, _ -> },
    onClickSelectThisFolder: () -> Unit = { },
    contextMenuItems: (ContentEntryAndListDetail) -> List<UstadContextMenuItem> = { emptyList() },
    onExportContentEntry: (Long) -> Unit = { }
) {
    val repositoryResult = rememberDoorRepositoryPager(
        uiState.contentEntryList, refreshCommandFlow
    )
    val lazyPagingItems = repositoryResult.lazyPagingItems

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        UstadFileDropZone(
            onFileDropped = onFileDropped,
            modifier = Modifier.weight(1f),
        ) {
            UstadLazyColumn(
                modifier = Modifier.fillMaxSize()
            )  {
                if(uiState.showChips) {
                    item(key = "filterchips") {
                        UstadListFilterChipsHeader(
                            filterOptions = uiState.filterOptions,
                            selectedChipId = uiState.selectedChipId,
                            onClickFilterChip = onClickFilterChip,
                        )
                    }
                }

                if(uiState.importFromFileItemVisible) {
                    item(key = "import_from_file_item") {
                        ListItem(
                            modifier = Modifier.clickable { onClickImportFile() },
                            headlineContent = {
                                Text(stringResource(MR.strings.import_from_file))
                            },
                            leadingContent = {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(40.dp),
                                ) {
                                    Icon(Icons.Default.FileUpload, contentDescription = null)
                                }
                            }
                        )
                    }
                }

                if(uiState.importFromLinkItemVisible) {
                    item(key = "import_from_link") {
                        ListItem(
                            modifier = Modifier.clickable { onClickImportFromLink() },
                            headlineContent = {
                                Text(stringResource(MR.strings.import_from_link))
                            },
                            leadingContent = {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(40.dp),
                                ) {
                                    Icon(Icons.Default.Link, contentDescription = null)
                                }
                            }
                        )
                    }
                }

                ustadPagedItems(
                    pagingItems = lazyPagingItems,
                    key = { contentEntry ->
                        Pair(contentEntry.contentEntry?.contentEntryUid ?: 0L, contentEntry.contentEntryParentChildJoin?.cepcjUid)
                    }
                ){ entry ->
                    val contentEntryUid = entry?.contentEntry?.contentEntryUid ?: 0
                    UstadContentEntryListItem(
                        onClick = {
                            onClickContentEntry(entry?.contentEntry)
                        },
                        entry = entry,
                        onSetSelected = onSetSelected,
                        isSelected = (contentEntryUid in uiState.selectedEntryUids),
                        contextMenuItems = { entryAndDetail ->
                            val defaultItems = contextMenuItems(entryAndDetail)
                            val exportItem = UstadContextMenuItem(
                                label = "EXport COntent",
                                onClick = { onExportContentEntry(entryAndDetail.contentEntry?.contentEntryUid ?: 0L) }
                            )
                            defaultItems + exportItem
                        },
                    )
                }
            }
        }

        if(uiState.showSelectFolderButton) {
            Button(
                modifier = Modifier.testTag("select_folder_button")
                    .fillMaxWidth()
                    .defaultItemPadding(),
                onClick = onClickSelectThisFolder,
            ) {
                Text(stringResource(MR.strings.move_entries_to_this_folder))
            }
        }

        if (uiState.exportProgress != null) {
            LinearProgressIndicator(
                progress = uiState.exportProgress!!.progress,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
            Text(
                text = "exporting",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}