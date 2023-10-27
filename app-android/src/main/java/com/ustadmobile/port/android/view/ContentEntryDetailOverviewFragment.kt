package com.ustadmobile.port.android.view

import android.view.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.themeadapter.material.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.entityconstants.ProgressConstants
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.progressBadge
import com.ustadmobile.core.viewmodel.contententry.detailoverviewtab.ContentEntryDetailOverviewUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.StatementEntity.Companion.RESULT_FAILURE
import com.ustadmobile.port.android.ui.theme.ui.theme.Typography
import com.ustadmobile.port.android.view.composable.UstadQuickActionButton
import com.ustadmobile.core.R as CR


interface ContentEntryDetailFragmentEventHandler {

    fun handleOnClickOpen()

    fun handleOnClickDownload()

    fun handleOnClickDeleteButton()

    fun handleOnClickManageDownloadButton()

    fun handleOnClickMarkComplete()
}

class ContentEntryDetailOverviewFragment:  UstadBaseMvvmFragment() {

}

@Composable
private fun ContentEntryDetailOverviewScreen(
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
                    Text(stringResource(CR.string.download).uppercase())
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
                    Text(stringResource(CR.string.open).uppercase())
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

        if (uiState.locallyAvailable) {
            item {
                LocallyAvailableRow()
            }
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
                Text(text = stringResource(id = CR.string.also_available_in))
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

        ContentDetailLeftColumn(uiState = uiState)

        ContentDetailRightColumn(uiState = uiState)
    }
}

@Composable
fun ContentDetailLeftColumn(
    uiState: ContentEntryDetailOverviewUiState = ContentEntryDetailOverviewUiState(),
){

    val badge = if (uiState.scoreProgress?.progressBadge() == ProgressConstants.BADGE_CHECK)
        R.drawable.ic_content_complete
    else
        R.drawable.ic_content_fail

    Column(
        modifier = Modifier.width(80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Image(painter = painterResource(id = R.drawable.book_24px),
            contentDescription = "",
            modifier = Modifier.height(90.dp),
            contentScale = ContentScale.Crop
        )

        BadgedBox(badge = {
            if (uiState.scoreProgress?.progressBadge() != ProgressConstants.BADGE_NONE){
                Image(
                    modifier = Modifier
                        .size(20.dp),
                    painter = painterResource(id = badge),
                    contentDescription = ""
                )
            }
        }) {
            if (uiState.scoreProgressVisible){
                LinearProgressIndicator(
                    progress = ((uiState.scoreProgress?.progress ?: 0)/100.0)
                        .toFloat(),
                    modifier = Modifier
                        .height(4.dp)
                        .padding(end = 10.dp)
                )
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
            text = uiState.contentEntry?.title ?: "",
            style = Typography.h4
        )

        if (uiState.authorVisible){
            Text(text = uiState.contentEntry?.author ?: "")
        }

        if (uiState.publisherVisible){
            Text(text = uiState.contentEntry?.publisher ?: "")
        }

        if (uiState.licenseNameVisible){
            Row{

                Text(text = stringResource(id = CR.string.entry_details_license))

                Spacer(modifier = Modifier.width(5.dp))

                Text(
                    text = uiState.contentEntry?.licenseName ?: "",
                    style = Typography.h6
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ){

            if (uiState.fileSizeVisible){
                Text(text = UMFileUtil.formatFileSize(
                    uiState.contentEntry?.container?.fileSize ?: 0
                ))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Row {
                Image(painter = painterResource(id = R.drawable.ic_baseline_emoji_events_24),
                    contentDescription = "",
                    modifier = Modifier.size(18.dp)
                )

                Text(uiState.scoreProgress?.progress.toString())
            }

            Spacer(modifier = Modifier.width(16.dp))

            if (uiState.scoreResultVisible){
                Text("(" +
                        (uiState.scoreProgress?.resultScore ?: "") +
                        "/" +
                        (uiState.scoreProgress?.resultMax ?: "") +
                        ")"
                )
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
fun LocallyAvailableRow(){
    Row{
        Image(painter = painterResource(id = R.drawable.ic_nearby_black_24px),
            contentDescription = "",
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(text = stringResource(id = CR.string.download_locally_availability))
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
                labelText = stringResource(id = CR.string.mark_complete).uppercase(),
                imageId = R.drawable.ic_checkbox_multiple_marked,
                onClick = onClickMarkComplete
            )
        }

        if (uiState.contentEntryButtons?.showDeleteButton == true){
            UstadQuickActionButton(
                labelText = stringResource(id = CR.string.delete).uppercase(),
                imageId = R.drawable.ic_delete_black_24dp,
                onClick = onClickDelete
            )
        }

        if (uiState.contentEntryButtons?.showManageDownloadButton == true){
            UstadQuickActionButton(
                labelText = stringResource(id = CR.string.manage_download).uppercase(),
                imageId = R.drawable.ic_file_download_black_24dp,
                onClick = onClickManageDownload
            )
        }
    }
}

@Composable
@Preview
fun ContentEntryDetailOverviewScreenPreview() {
    val uiStateVal = ContentEntryDetailOverviewUiState(
        contentEntry = ContentEntryWithMostRecentContainer().apply {
            title = "Content Title"
            author = "Author"
            publisher = "Publisher"
            licenseName = "BY_SA"
            container = Container().apply {
                fileSize = 50
            }
            description = "Content Description"
        },
        scoreProgress = ContentEntryStatementScoreProgress().apply {
            /*@FloatRange(from = 0.0, to = 1.0)*/
            progress = 4

            success = RESULT_FAILURE
            resultScore = 4
            resultMax = 40
        },
        contentEntryButtons = ContentEntryButtonModel().apply {
            showDownloadButton = true
            showOpenButton = true
            showDeleteButton = true
            showManageDownloadButton = true
        },
        availableTranslations = listOf(
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 0
                    name = "Persian"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 1
                    name = "English"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 2
                    name = "Korean"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 3
                    name = "Tamil"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 4
                    name = "Turkish"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 5
                    name = "Telugu"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 6
                    name = "Marathi"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 7
                    name = "Vietnamese"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 8
                    name = "Japanese"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 9
                    name = "Russian"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 10
                    name = "Portuguese"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 11
                    name = "Bengali"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 12
                    name = "Spanish"
                }
            },
            ContentEntryRelatedEntryJoinWithLanguage().apply {
                language = Language().apply {
                    langUid = 13
                    name = "Hindi"
                }
            }
        ),
        activeContentJobItems = listOf(
            ContentJobItemProgress().apply {
                cjiUid = 0
                progressTitle = "First"
                progress = 30
            },
            ContentJobItemProgress().apply {
                cjiUid = 1
                progressTitle = "Second"
                progress = 10
            },
            ContentJobItemProgress().apply {
                cjiUid = 2
                progressTitle = "Third"
                progress = 70
            }
        ),
        locallyAvailable = true,
        markCompleteVisible = true,
        translationVisibile = true
    )
    MdcTheme {
        ContentEntryDetailOverviewScreen(
            uiState = uiStateVal
        )
    }
}