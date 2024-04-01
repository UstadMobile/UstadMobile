package com.ustadmobile.libuicompose.view.contententry.detailoverviewtab

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.contententry.detailoverviewtab.ContentEntryDetailOverviewUiState
import com.ustadmobile.core.viewmodel.contententry.detailoverviewtab.ContentEntryDetailOverviewViewModel
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItemProgress
import com.ustadmobile.libuicompose.components.UstadHtmlText
import com.ustadmobile.libuicompose.components.UstadLazyColumn
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
    onClickContentJobItem: () -> Unit = {},
) {
    UstadLazyColumn(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .fillMaxSize()
    )  {

        item("details") {
            ContentDetails(
                uiState = uiState
            )
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
            Divider(thickness = 1.dp)
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
            key = { item -> item.transferJob?.tjUid ?: 0 }
        ) { item ->
            ListItem(
                headlineContent = {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        progress = (item.transferred.toFloat() / max(item.totalSize.toFloat(), 1f))
                    )
                },
                supportingContent = {
                    Text(stringResource(MR.strings.uploading))
                }
            )
        }

        items(
            items = uiState.activeContentJobItems,
            key = { contentJob -> contentJob.cjiUid }
        ){ contentJobItem ->
            ContentJobListItem(
                uiState = uiState,
                onClickContentJobItem = onClickContentJobItem,
                contentJob = contentJobItem
            )
        }


        item {
            Divider(thickness = 1.dp)
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
fun ContentDetails(
    uiState: ContentEntryDetailOverviewUiState = ContentEntryDetailOverviewUiState(),
){
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        ContentDetailLeftColumn()

        ContentDetailRightColumn(uiState = uiState)
    }
}

@Composable
fun ContentDetailLeftColumn(){
    Column(
        modifier = Modifier.width(80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Icon(
            imageVector = Icons.Default.Book,
            contentDescription = "",
            modifier = Modifier.size(80.dp),
        )
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

        if (uiState.authorVisible){
            Text(text = uiState.contentEntry?.entry?.author ?: "")
        }

        if (uiState.publisherVisible){
            Text(text = uiState.contentEntry?.entry?.publisher ?: "")
        }

        if (uiState.licenseNameVisible){
            Row{

                Text(text = stringResource(MR.strings.entry_details_license))

                Spacer(modifier = Modifier.width(5.dp))

                Text(text = uiState.contentEntry?.entry?.licenseName ?: "")
            }
        }
    }
}

@Composable
fun ContentJobListItem(
    uiState: ContentEntryDetailOverviewUiState,
    onClickContentJobItem: () -> Unit,
    contentJob: ContentJobItemProgress
){
    ListItem(
        modifier = Modifier.clickable {
            onClickContentJobItem()
        },
        headlineContent = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(contentJob.progressTitle ?: "")
                Text(contentJob.progress.toString()+" %")
            }
        },
        supportingContent = {
            LinearProgressIndicator(
                progress = (contentJob.progress/100.0).toFloat(),
                modifier = Modifier
                    .height(4.dp),
            )
        }
    )
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
