package com.ustadmobile.libuicompose.view.contententry.detail

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.contententry.detailoverviewtab.ContentEntryDetailOverviewUiState
import com.ustadmobile.lib.db.entities.ContentEntryRelatedEntryJoinWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItemProgress
import com.ustadmobile.libuicompose.components.UstadQuickActionButton
import dev.icerock.moko.resources.compose.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContentEntryDetailOverviewScreen(
    uiState: ContentEntryDetailOverviewUiState = ContentEntryDetailOverviewUiState(),
    onClickDownload: () -> Unit = {},
    onClickOpen: () -> Unit = {},
    onClickMarkComplete: () -> Unit = {},
    onClickDelete: () -> Unit = {},
    onClickManageDownload: () -> Unit = {},
    onClickTranslation: (ContentEntryRelatedEntryJoinWithLanguage) -> Unit = {},
    onClickContentJobItem: () -> Unit = {},
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )  {

        item {
            ContentDetails(
                uiState = uiState
            )
        }

        if (uiState.contentEntryButtons?.showDownloadButton == true){
            item {
                Button(
                    onClick = onClickDownload,
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Text(stringResource(MR.strings.download).uppercase())
                }
            }
        }

        if (uiState.contentEntryButtons?.showOpenButton == true){
            item {
                Button(
                    onClick = onClickOpen,
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    Text(stringResource(MR.strings.open).uppercase())
                }
            }
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
            Text(text = uiState.contentEntry?.description ?: "")
        }

        item {
            Divider(thickness = 1.dp)
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
        Image(
            imageVector = Icons.Default.Book,
            contentDescription = "",
            modifier = Modifier.height(90.dp),
            contentScale = ContentScale.Crop
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
            text = uiState.contentEntry?.title ?: "",
            style = MaterialTheme.typography.headlineLarge
        )

        if (uiState.authorVisible){
            Text(text = uiState.contentEntry?.author ?: "")
        }

        if (uiState.publisherVisible){
            Text(text = uiState.contentEntry?.publisher ?: "")
        }

        if (uiState.licenseNameVisible){
            Row{

                Text(text = stringResource(MR.strings.entry_details_license))

                Spacer(modifier = Modifier.width(5.dp))

                Text(text = uiState.contentEntry?.licenseName ?: "")
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
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
        text = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(contentJob.progressTitle ?: "")
                Text(contentJob.progress.toString()+" %")
            }
        },
        secondaryText = {
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
