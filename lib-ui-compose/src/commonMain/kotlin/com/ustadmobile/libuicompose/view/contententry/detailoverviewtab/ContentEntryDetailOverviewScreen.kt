package com.ustadmobile.libuicompose.view.contententry.detailoverviewtab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.viewmodel.contententry.detailoverviewtab.ContentEntryDetailOverviewUiState
import com.ustadmobile.core.viewmodel.contententry.detailoverviewtab.ContentEntryDetailOverviewViewModel
import com.ustadmobile.core.viewmodel.contententry.detailoverviewtab.progress
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.lib.db.entities.TransferJob
import com.ustadmobile.libuicompose.components.UstadBlockStatusProgressBar
import com.ustadmobile.libuicompose.components.UstadBlockIcon
import com.ustadmobile.libuicompose.components.UstadHtmlText
import com.ustadmobile.libuicompose.components.UstadLazyColumn
import com.ustadmobile.libuicompose.components.UstadLinearProgressListItem
import com.ustadmobile.libuicompose.components.UstadOfflineItemStatusQuickActionButton
import com.ustadmobile.libuicompose.components.UstadQuickActionButton
import com.ustadmobile.libuicompose.util.ext.defaultItemPadding
import dev.icerock.moko.resources.compose.stringResource
import kotlin.math.max

@Composable
fun ContentEntryDetailOverviewScreen(
    viewModel: ContentEntryDetailOverviewViewModel
) {
    val uiState by viewModel.uiState.collectAsState(
        ContentEntryDetailOverviewUiState()
    )

    ContentEntryDetailOverviewScreen(
        uiState = uiState,
        onClickOpen = viewModel::onClickOpen,
        onClickOfflineButton = viewModel::onClickOffline,
        onCancelImport = viewModel::onCancelImport,
        onCancelRemoteImport = viewModel::onCancelRemoteImport,
        onDismissImportError = viewModel::onDismissImportError,
        onDismissRemoteImportError = viewModel::onDismissRemoteImportError,
        onDismissUploadError = viewModel::onDismissUploadError,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContentEntryDetailOverviewScreen(
    uiState: ContentEntryDetailOverviewUiState = ContentEntryDetailOverviewUiState(),
    onClickOfflineButton: () -> Unit = {},
    onClickOpen: () -> Unit = {},
    onClickMarkComplete: () -> Unit = {},
    onClickDelete: () -> Unit = {},
    onClickManageDownload: () -> Unit = {},
    onClickTranslation: (ContentEntryRelatedEntryJoinWithLanguage) -> Unit = {},
    onCancelImport: (Long) -> Unit = { },
    onCancelRemoteImport: (Long) -> Unit = { },
    onDismissImportError: (Long) -> Unit = { },
    onDismissRemoteImportError: (Long) -> Unit = { },
    onDismissUploadError: (Int) -> Unit = { },
) {
    UstadLazyColumn(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .fillMaxSize()
    )  {

        item("details") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Box(Modifier.defaultItemPadding().size(80.dp)) {
                    UstadBlockIcon(
                        title = uiState.contentEntry?.entry?.title ?: "",
                        contentEntry = uiState.contentEntry?.entry,
                        pictureUri = uiState.contentEntry?.picture?.cepPictureUri,
                        modifier = Modifier.size(80.dp),
                    )

                    UstadBlockStatusProgressBar(
                        blockStatus = uiState.contentEntry?.status,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }

                ContentDetailRightColumn(uiState = uiState)
            }
        }

        if (uiState.openButtonVisible){
            item(key = "open_button") {
                Button(
                    onClick = onClickOpen,
                    modifier = Modifier.fillMaxWidth().defaultItemPadding().testTag("open_content_button"),
                ) {
                    Text(stringResource(MR.strings.open))
                }
            }
        }

        item(key = "upper_divider") {
            HorizontalDivider(thickness = 1.dp)
        }

        item("quick_action_row") {
            Row(
                modifier = Modifier.fillMaxWidth().defaultItemPadding()
            ) {
                UstadOfflineItemStatusQuickActionButton(
                    state = uiState.offlineItemAndState,
                    onClick = onClickOfflineButton,
                    modifier = Modifier.testTag("offline_status_button")
                )
            }
        }

        items(
            items = uiState.activeUploadJobs,
            key = { item -> Pair(TransferJob.TABLE_ID, item.transferJob?.tjUid ?: 0) }
        ) { item ->
            UstadLinearProgressListItem(
                progress = (item.transferred.toFloat() / max(item.totalSize.toFloat(), 1f)),
                supportingContent = {
                    Text(stringResource(MR.strings.uploading))
                },
                onCancel = {

                },
                error = item.latestErrorStr,
                errorTitle = stringResource(MR.strings.upload_error),
                onDismissError = {
                    onDismissUploadError(item.transferJob?.tjUid ?: 0)
                }
            )
        }

        items(
            items = uiState.activeImportJobs,
            key = { Pair(ContentEntryImportJob.TABLE_ID, it.cjiUid) }
        ){ contentJobItem ->
            UstadLinearProgressListItem(
                progress = contentJobItem.progress,
                supportingContent = {
                    Text(stringResource(MR.strings.importing))
                },
                onCancel = {
                    onCancelImport(contentJobItem.cjiUid)
                },
                error = contentJobItem.cjiError,
                onDismissError = {
                    onDismissImportError(contentJobItem.cjiUid)
                }
            )
        }

        items(
            items = uiState.remoteImportJobs,
            key = { Pair("remoteimport", it.cjiUid) }
        ) {contentJobItem ->
            val canCancel = uiState.canCancelRemoteImportJob(contentJobItem)

            UstadLinearProgressListItem(
                progress = contentJobItem.progress,
                supportingContent = {
                    Text(stringResource(MR.strings.importing))
                },
                onCancel = if(canCancel) {
                    { onCancelRemoteImport(contentJobItem.cjiUid) }
                }else {
                    null
                },
                error = contentJobItem.cjiError,
                onDismissError = if(canCancel) {
                    { onDismissRemoteImportError(contentJobItem.cjiUid) }
                }else {
                    null
                }
            )
        }

        item {
            HorizontalDivider(thickness = 1.dp)
        }

        item {
            QuickActionBarsRow(
                uiState = uiState,
                onClickMarkComplete = onClickMarkComplete,
                onClickDelete = onClickDelete,
                onClickManageDownload = onClickManageDownload
            )
        }

        item {
            UstadHtmlText(
                html = uiState.contentEntry?.entry?.description ?: "",
                modifier = Modifier.defaultItemPadding()
            )
        }

        if (uiState.translationVisibile){
            item {
                Text(text = stringResource(MR.strings.also_available_in))
            }

            item {
                FlowRow(
                    Modifier.padding(8.dp)
                ) {
                    uiState.availableTranslations.forEach { translation ->
                        TextButton(onClick = { onClickTranslation(translation) }) {
                            Text(text = translation.language?.name ?: "")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContentDetailRightColumn(
    uiState: ContentEntryDetailOverviewUiState = ContentEntryDetailOverviewUiState(),
){
    Column(
        modifier = Modifier.height(IntrinsicSize.Max),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {

        Text(
            text = uiState.contentEntry?.entry?.title ?: "",
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 2,
        )

        if(uiState.compressedSizeVisible) {
            Text(
                text = stringResource(
                    MR.strings.size_compressed_was,
                    UMFileUtil.formatFileSize(uiState.latestContentEntryVersion?.cevStorageSize ?: 0),
                    UMFileUtil.formatFileSize(uiState.latestContentEntryVersion?.cevOriginalSize ?: 0),
                ),
                style = MaterialTheme.typography.labelSmall,
            )
        }else if(uiState.sizeVisible) {
            Text(
                text = stringResource(
                    MR.strings.size,
                    UMFileUtil.formatFileSize(uiState.latestContentEntryVersion?.cevStorageSize ?: 0),
                ),
                style = MaterialTheme.typography.labelSmall,
            )
        }

        if (uiState.authorVisible){
            Text(
                text = uiState.contentEntry?.entry?.author ?: "",
                style = MaterialTheme.typography.labelSmall,
            )
        }

        if (uiState.publisherVisible){
            Text(
                text = uiState.contentEntry?.entry?.publisher ?: "",
                style = MaterialTheme.typography.labelSmall,
            )
        }

        if (uiState.licenseNameVisible){
            Row{

                Text(
                    text = stringResource(MR.strings.entry_details_license),
                    style = MaterialTheme.typography.labelSmall,
                )

                Spacer(modifier = Modifier.width(5.dp))

                Text(
                    text = uiState.contentEntry?.entry?.licenseName ?: "",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
fun QuickActionBarsRow(
    uiState: ContentEntryDetailOverviewUiState,
    onClickMarkComplete: () -> Unit = {},
    onClickDelete: () -> Unit = {},
    onClickManageDownload: () -> Unit = {}
){
    Row {
        if (uiState.markCompleteVisible){
            UstadQuickActionButton(
                imageVector = Icons.Default.CheckBox,
                labelText = stringResource(MR.strings.mark_complete),
                onClick = onClickMarkComplete
            )
        }

        if (uiState.contentEntryButtons?.showDeleteButton == true){
            UstadQuickActionButton(
                labelText = stringResource(MR.strings.delete),
                imageVector = Icons.Default.Delete,
                onClick = onClickDelete,
            )
        }

        if (uiState.contentEntryButtons?.showManageDownloadButton == true){
            UstadQuickActionButton(
                labelText = stringResource(MR.strings.manage_download),
                imageVector = Icons.Default.FileDownload,
                onClick = onClickManageDownload,
            )
        }
    }
}
