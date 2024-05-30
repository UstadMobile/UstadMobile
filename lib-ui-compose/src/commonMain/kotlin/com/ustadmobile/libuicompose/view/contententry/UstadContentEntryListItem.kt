package com.ustadmobile.libuicompose.view.contententry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.impl.appstate.UstadContextMenuItem
import com.ustadmobile.core.viewmodel.contententry.contentTypeStringResource
import com.ustadmobile.lib.db.composites.ContentEntryAndListDetail
import com.ustadmobile.libuicompose.components.UstadBlockIcon
import com.ustadmobile.libuicompose.components.UstadBlockStatusProgressBar
import com.ustadmobile.libuicompose.components.UstadContextMenuArea
import com.ustadmobile.libuicompose.components.UstadSelectableListItem
import com.ustadmobile.libuicompose.components.UstadSelectedIcon
import com.ustadmobile.libuicompose.util.rememberHtmlToPlainText
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun UstadContentEntryListItem(
    modifier: Modifier = Modifier,
    entry: ContentEntryAndListDetail?,
    onClick: () -> Unit = { },
    isSelected: Boolean = false,
    contextMenuItems: (ContentEntryAndListDetail) -> List<UstadContextMenuItem> = { emptyList() },
    onSetSelected: (contentEntry: ContentEntryAndListDetail, selected: Boolean) -> Unit = { _, _ -> },
) {
    val descriptionPlainText = rememberHtmlToPlainText(
        entry?.contentEntry?.description ?: ""
    )
    UstadContextMenuArea(
        items = {
            entry?.let { contextMenuItems(it) } ?: emptyList()
        }
    ) {
        UstadSelectableListItem(
            modifier = modifier,
            isSelected = isSelected,
            onClick = onClick,
            onSetSelected = {
                entry?.also { onSetSelected(it, !isSelected) }
            },
            headlineContent = {
                Text(
                    text = entry?.contentEntry?.title ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingContent = {
                if(isSelected) {
                    UstadSelectedIcon()
                }else {
                    Box(
                        Modifier.size(40.dp)
                    ) {
                        UstadBlockIcon(
                            title = entry?.contentEntry?.title ?: "",
                            contentEntry = entry?.contentEntry,
                            courseBlock = null,
                            pictureUri = entry?.picture?.cepThumbnailUri,
                        )

                        UstadBlockStatusProgressBar(
                            blockStatus = entry?.status,
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    }
                }

            },
            supportingContent = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if(descriptionPlainText.isNotBlank()) {
                        Text(
                            text = descriptionPlainText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Row {
                        entry?.contentEntry?.also { contentEntry ->
                            Icon(contentEntry.contentTypeImageVector,
                                contentDescription = "",
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            entry.contentEntry?.contentTypeStringResource?.also {
                                Text(stringResource(it))
                            }
                        }
                    }
                }
            },
        )
    }

}

