package com.ustadmobile.libuicompose.view.contententry

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Book
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.contententry.contentTypeStringResource
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListItemUiState
import com.ustadmobile.core.viewmodel.contententry.list.listItemUiState
import com.ustadmobile.lib.db.entities.*
import dev.icerock.moko.resources.compose.stringResource
import com.ustadmobile.libuicompose.view.contententry.list.ClazzAssignmentConstants.CONTENT_ENTRY_TYPE_ICON_MAP

@Composable
fun UstadContentEntryListItem(
    modifier: Modifier = Modifier,
    contentEntry: ContentEntry?,
    onClick: () -> Unit = { },
) {

    val uiState = contentEntry?.listItemUiState
    ListItem(
        modifier = modifier
            .alpha((uiState?.containerAlpha ?: 0.0).toFloat())
            .clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = uiState?.contentEntry?.title ?: "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            LeadingContent(
                uiState = uiState,
                contentEntry = uiState?.contentEntry
            )
        },
        supportingContent = {
            SecondaryContent(
                contentEntry = uiState?.contentEntry,
                uiState = uiState
            )
        },
    )

}

@Composable
private fun LeadingContent(
    uiState: ContentEntryListItemUiState?,
    contentEntry: ContentEntry?
){
    val thumbnail: ImageVector = if (contentEntry?.leaf == true)
        Icons.Outlined.Book
    else
        Icons.Default.Folder

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ){
        Icon(
            thumbnail,
            contentDescription = "",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(40.dp)
                .padding(4.dp),
        )
    }
}

@Composable
private fun SecondaryContent(
    contentEntry: ContentEntry?,
    uiState: ContentEntryListItemUiState?
){
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (uiState?.descriptionVisible == true){
            Text((contentEntry?.description ?: ""), maxLines = 2)
        }

        Row {
            val contentTypeFlagVal = contentEntry?.contentTypeFlag
            if (uiState?.mimetypeVisible == true && contentTypeFlagVal != null){
                Icon(CONTENT_ENTRY_TYPE_ICON_MAP[contentTypeFlagVal] ?: Icons.Filled.Book,
                    contentDescription = "",
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(stringResource(contentEntry.contentTypeStringResource))
            }
        }
    }
}

