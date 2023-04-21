package com.ustadmobile.port.android.view.composable

import android.view.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Book
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.toughra.ustadmobile.R
import com.ustadmobile.core.entityconstants.ProgressConstants
import com.ustadmobile.core.impl.locale.entityconstants.ContentEntryTypeLabelConstants
import com.ustadmobile.core.util.ext.progressBadge
import com.ustadmobile.core.viewmodel.ContentEntryListItemUiState
import com.ustadmobile.core.viewmodel.listItemUiState
import com.ustadmobile.lib.db.entities.*
import com.ustadmobile.lib.db.entities.StatementEntity.Companion.RESULT_SUCCESS
import com.ustadmobile.port.android.util.compose.messageIdResource
import com.ustadmobile.port.android.view.ContentEntryDetailOverviewFragment.Companion.CONTENT_ENTRY_TYPE_ICON_MAP

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UstadContentEntryListItem(
    modifier: Modifier = Modifier,
    contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer,
    onClick: () -> Unit = { },
    onClickDownload: () -> Unit = { },
) {

    val uiState = contentEntry.listItemUiState
    ListItem(
        modifier = modifier
            .alpha((uiState.containerAlpha).toFloat())
            .clickable(onClick = onClick),
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
                onClick = onClickDownload,
                contentEntry = uiState.contentEntry
            )
        }
    )

}

@Composable
private fun LeadingContent(
    uiState: ContentEntryListItemUiState,
    contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
){

    val thumbnail: ImageVector = if (contentEntry.leaf)
        Icons.Outlined.Book
    else
        Icons.Default.Folder

    var badgeColor = colorResource(R.color.errorColor)
    var badge = Icons.Default.Cancel
    if (contentEntry.scoreProgress?.progressBadge() == ProgressConstants.BADGE_CHECK) {
        badge = Icons.Default.CheckCircle
        badgeColor = colorResource(R.color.successColor)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.End
    ){
        Icon(
            thumbnail,
            contentDescription = "",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(45.dp)
                .padding(4.dp),
        )

        BadgedBox(badge = {
            if (contentEntry.scoreProgress?.progressBadge() != ProgressConstants.BADGE_NONE){
                Icon(
                    badge,
                    contentDescription = "",
                    modifier = Modifier.size(15.dp),
                    tint = badgeColor
                )
            }
        }) {
            if (uiState.progressVisible){
                Box(
                    modifier = Modifier
                        .width(45.dp)
                        .height(15.dp)
                        .padding(end = 10.dp)
                ) {
                    LinearProgressIndicator(
                        progress = ((contentEntry.scoreProgress?.progress ?: 0)/100.0)
                            .toFloat(),
                        modifier = Modifier
                            .height(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SecondaryContent(
    contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer,
    uiState: ContentEntryListItemUiState
){
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (uiState.descriptionVisible){
            Text((contentEntry.description ?: ""))
        }

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
private fun SecondaryAction(
    onClick: () -> Unit,
    contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer
){
    IconButton(
        onClick = onClick,
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
    val contentEntry = ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer()
            .apply {
                contentEntryUid = 1
                leaf = true
                ceInactive = true
                scoreProgress = ContentEntryStatementScoreProgress().apply {
                    progress = 10
                    penalty = 20
                    success = RESULT_SUCCESS
                }
                contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                title = "Content Title"
                description = "Content Description"
            }
    MdcTheme {
        UstadContentEntryListItem(
            contentEntry = contentEntry
        )
    }
}