package com.ustadmobile.port.android.view.composable

import android.view.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Book
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.entityconstants.ProgressConstants
import com.ustadmobile.core.impl.locale.entityconstants.ContentEntryTypeLabelConstants
import com.ustadmobile.core.util.ext.progressBadge
import com.ustadmobile.core.viewmodel.ContentEntryListItemUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.StatementEntity.Companion.RESULT_SUCCESS
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.view.ContentEntryDetailOverviewFragment.Companion.CONTENT_ENTRY_TYPE_ICON_MAP

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun UstadContentEntryListItem(
    uiState: ContentEntryListItemUiState = ContentEntryListItemUiState(),
    onClickContentEntry: (ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) -> Unit = {},
    onClickDownloadContentEntry: (ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) -> Unit = {},
) {

    ListItem(
        modifier = Modifier
            .alpha((uiState.containerAlpha).toFloat())
            .clickable {
                onClickContentEntry(uiState.contentEntry)
            },

        text = { Text(uiState.contentEntry.title ?: "") },
        icon = {
            LeadingContent(
                uiState = uiState,
                contentEntry = uiState.contentEntry
            )
        },
        secondaryText = {
            SecondaryContent(
                contentEntry = uiState.contentEntry,
                uiState = uiState
            )
        },
        trailing = {
            SecondaryAction(
                onClick = onClickDownloadContentEntry,
                contentEntry = uiState.contentEntry
            )
        }
    )

}

@Composable
fun LeadingContent(
    uiState: ContentEntryListItemUiState,
    contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
){

    val thumbnail: ImageVector = if (contentEntry.leaf)
        Icons.Outlined.Book
    else
        Icons.Default.Folder

    val badge = if (contentEntry.scoreProgress?.progressBadge() == ProgressConstants.BADGE_CHECK)
        R.drawable.ic_content_complete
    else
        R.drawable.ic_content_fail

    Column(
        modifier = Modifier.width(45.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.End
    ){
        Box(modifier = Modifier
            .size(35.dp)
            .align(Alignment.CenterHorizontally)
            .border(
                BorderStroke(1.dp, MaterialTheme.colors.onSurface), CircleShape
            ),
            contentAlignment = Alignment.Center
        ){
            Icon(
                thumbnail,
                contentDescription = "",
                modifier = Modifier
                    .padding(4.dp),
            )
        }

        BadgedBox(badge = {
            if (contentEntry.scoreProgress?.progressBadge() != ProgressConstants.BADGE_NONE){
                Image(
                    modifier = Modifier
                        .size(25.dp),
                    painter = painterResource(id = badge),
                    contentDescription = ""
                )
            }
        }) {
            if (uiState.progressVisible){
                LinearProgressIndicator(
                    progress = ((contentEntry.scoreProgress?.progress ?: 0)/100.0)
                        .toFloat(),
                    modifier = Modifier
                        .height(4.dp)
                        .padding(end = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun SecondaryContent(
    contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer,
    uiState: ContentEntryListItemUiState
){
    Column {
        Text((contentEntry.description ?: ""))

        Spacer(modifier = Modifier.height(5.dp))

        Row {

            if (uiState.mimetypeVisible){
                Image(painter = painterResource(id =
                CONTENT_ENTRY_TYPE_ICON_MAP[contentEntry.contentTypeFlag]
                    ?: ContentEntry.TYPE_EBOOK),
                    contentDescription = "",
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    messageIdResource(id = ContentEntryTypeLabelConstants
                        .TYPE_LABEL_MESSAGE_IDS[contentEntry.contentTypeFlag]
                        .messageId)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = ""
            )

            Text("${contentEntry.scoreProgress?.progress ?: 0}%")

            Text(uiState.scoreResultText)
        }
    }
}

@Composable
fun SecondaryAction(
    onClick: (ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer) -> Unit,
    contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
){
    IconButton(
        onClick = { onClick(contentEntry) },
    ) {

        CircularProgressIndicator(
            progress = ((contentEntry.scoreProgress?.progress ?: 0) / 100.0)
                .toFloat(),
            color = MaterialTheme.colors.secondary
        )

        Icon(
            Icons.Filled.FileDownload,
            contentDescription = ""
        )
    }

}

@Composable
@Preview
private fun ContentEntryListScreenPreview() {
    val uiStateVal = ContentEntryListItemUiState(
        contentEntry = ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer()
            .apply {
                contentEntryUid = 1
                leaf = false
                ceInactive = true
                scoreProgress = ContentEntryStatementScoreProgress().apply {
                    progress = 10
                    penalty = 20
                    success = RESULT_SUCCESS
                }
                contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                title = "Content Title 1"
                description = "Content Description 1"
            },
    )
    MdcTheme {
        UstadContentEntryListItem(uiStateVal)
    }
}