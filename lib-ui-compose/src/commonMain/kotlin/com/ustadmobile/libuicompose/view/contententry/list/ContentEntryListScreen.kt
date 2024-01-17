package com.ustadmobile.libuicompose.view.contententry.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListUiState
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListViewModel
import com.ustadmobile.libuicompose.view.contententry.UstadContentEntryListItem
import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import com.ustadmobile.libuicompose.components.ustadPagedItems
import androidx.compose.runtime.remember
import androidx.paging.compose.collectAsLazyPagingItems
import com.ustadmobile.libuicompose.components.UstadBottomSheetOption
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.libuicompose.components.UstadFileDropZone
import com.ustadmobile.libuicompose.components.UstadFilePickResult
import com.ustadmobile.libuicompose.components.UstadPickFileOpts
import com.ustadmobile.libuicompose.components.rememberUstadFilePickLauncher

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
        }
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
                    filePickLauncher(UstadPickFileOpts())
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
    onClickContentEntry: (ContentEntry?) -> Unit = {},
    onFileDropped: (UstadFilePickResult) -> Unit = { },
) {
    val pager = remember(uiState.contentEntryList) {
        Pager(
            pagingSourceFactory = uiState.contentEntryList,
            config = PagingConfig(pageSize = 20, enablePlaceholders = true)
        )
    }
    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()

    UstadFileDropZone(
        onFileDropped = onFileDropped,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        )  {
            ustadPagedItems(
                pagingItems = lazyPagingItems,
                key = { contentEntry -> contentEntry.contentEntryUid }
            ){ contentEntry ->
                UstadContentEntryListItem(
                    onClick = {
                        onClickContentEntry(contentEntry)
                    },
                    contentEntry = contentEntry
                )
            }
        }
    }
}