package com.ustadmobile.libuicompose.view.contententry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.contententry.contentTypeStringResource
import com.ustadmobile.core.viewmodel.contententry.list.ContentEntryListItemUiState
import com.ustadmobile.core.viewmodel.contententry.list.listItemUiState
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.libuicompose.components.UstadSelectableListItem
import com.ustadmobile.libuicompose.components.UstadSelectedIcon
import com.ustadmobile.libuicompose.view.contententry.list.ClazzAssignmentConstants.CONTENT_ENTRY_TYPE_ICON_MAP
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun UstadContentEntryListItem(
    modifier: Modifier = Modifier,
    contentEntry: ContentEntry?,
    onClick: () -> Unit = { },
    isSelected: Boolean = false,
    onSetSelected: (contentEntryUid: Long, selected: Boolean) -> Unit = { _, _ -> },
) {
    val uiState = contentEntry?.listItemUiState

    UstadSelectableListItem(
        modifier = modifier,
        isSelected = isSelected,
        onClick = onClick,
        onSetSelected = {
            onSetSelected(contentEntry?.contentEntryUid ?: 0, it)
        },
        headlineContent = {
            Text(
                text = uiState?.contentEntry?.title ?: "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            if(isSelected) {
                UstadSelectedIcon()
            }else {
                val thumbnail: ImageVector = if (contentEntry?.leaf == true)
                    Icons.Outlined.Book
                else
                    Icons.Default.Folder
                Icon(
                    thumbnail,
                    contentDescription = "",
                    modifier = Modifier.size(40.dp).padding(4.dp),
                )
            }

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

