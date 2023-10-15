package com.ustadmobile.libuicompose.view.contententry

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
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.entityconstants.ProgressConstants
import com.ustadmobile.core.impl.locale.entityconstants.ContentEntryTypeLabelConstants
import com.ustadmobile.core.util.ext.progressBadge
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListItemUiState
import com.ustadmobile.core.viewmodel.contententry.list.listItemUiState
import com.ustadmobile.lib.db.entities.*
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.core.MR

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UstadContentEntryListItem(
    modifier: Modifier = Modifier,
    contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?,
    onClick: () -> Unit = { },
    onClickDownload: () -> Unit = { },
) {

    val uiState = contentEntry?.listItemUiState
    ListItem(
        modifier = modifier
            .alpha((uiState?.containerAlpha ?: 0.0).toFloat())
            .clickable(onClick = onClick),
        text = { Text(uiState?.contentEntry?.title ?: "") },
        icon = {
            LeadingContent(
                uiState = uiState,
                contentEntry = uiState?.contentEntry
            )
        },
        secondaryText = {
            SecondaryContent(
                contentEntry = uiState?.contentEntry,
                uiState = uiState
            )
        },
        /*
        To be enabled when reactive sync is enabled
        trailing = {
            SecondaryAction(
                onClick = onClickDownload,
                contentEntry = uiState?.contentEntry
            )
        }
         */
    )

}

@Composable
private fun LeadingContent(
    uiState: ContentEntryListItemUiState?,
    contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?
){

    val thumbnail: ImageVector = if (contentEntry?.leaf == true)
        Icons.Outlined.Book
    else
        Icons.Default.Folder

    var badgeColor = MaterialTheme.colors.error
    var badge = Icons.Default.Cancel
    if (contentEntry?.scoreProgress?.progressBadge() == ProgressConstants.BADGE_CHECK) {
        badge = Icons.Default.CheckCircle
        //  TODO error
//        badgeColor = colorResource(R.color.successColor)
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
            if (contentEntry?.scoreProgress?.progressBadge() != ProgressConstants.BADGE_NONE){
                Icon(
                    badge,
                    contentDescription = "",
                    modifier = Modifier.size(15.dp),
                    tint = badgeColor
                )
            }
        }) {
            if (uiState?.progressVisible == true){
                Box(
                    modifier = Modifier
                        .width(45.dp)
                        .height(15.dp)
                        .padding(end = 10.dp)
                ) {
                    LinearProgressIndicator(
                        progress = ((contentEntry?.scoreProgress?.progress ?: 0)/100.0)
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
    contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?,
    uiState: ContentEntryListItemUiState?
){
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (uiState?.descriptionVisible == true){
            Text((contentEntry?.description ?: ""))
        }

        Row {
            val contentTypeFlagVal = contentEntry?.contentTypeFlag
            if (uiState?.mimetypeVisible == true && contentTypeFlagVal != null){
//                Image(painter = painterResource(id =
                //  TODO error
//                CONTENT_ENTRY_TYPE_ICON_MAP[contentTypeFlagVal] ?: ContentEntry.TYPE_EBOOK),
//                    contentDescription = "",
//                    modifier = Modifier.size(20.dp)
//                )

                Text(
                    stringResource(resource = ContentEntryTypeLabelConstants
                        .TYPE_LABEL_MESSAGE_IDS[contentTypeFlagVal]
                        .stringResource)
                )
            }

            /*
            Restore after reactive sync
            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = ""
            )

            Text("${contentEntry?.scoreProgress?.progress ?: 0}%")

            Text(uiState?.scoreResultText ?: "")
             */
        }
    }
}

@Composable
private fun SecondaryAction(
    onClick: () -> Unit,
    contentEntry: ContentEntryWithParentChildJoinAndStatusAndMostRecentContainer?
){
    IconButton(
        onClick = onClick,
    ) {

        CircularProgressIndicator(
            progress = ((contentEntry?.scoreProgress?.progress ?: 0) / 100.0)
                .toFloat(),
            color = MaterialTheme.colors.secondary
        )

        Icon(
            Icons.Filled.FileDownload,
            contentDescription = stringResource(MR.strings.download)
        )
    }
}