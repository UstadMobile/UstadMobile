package com.ustadmobile.libuicompose.view.deleteditem.list

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.MR
import com.ustadmobile.core.viewmodel.deleteditem.delItemContentTypeStringResource
import com.ustadmobile.lib.db.entities.DeletedItem
import com.ustadmobile.libuicompose.components.UstadTooltipBox
import com.ustadmobile.libuicompose.util.rememberFormattedDateTime
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.datetime.TimeZone

@Composable
fun DeletedItemListItem(
    deletedItem: DeletedItem?,
    onClickRestore: (DeletedItem) -> Unit = { },
    onClickDeletePermanently: (DeletedItem) -> Unit = { }
) {
    val deletedTime = rememberFormattedDateTime(
        timeInMillis = deletedItem?.delItemTimeDeleted ?: 0,
        timeZoneId = TimeZone.currentSystemDefault().id,
    )

    ListItem(
        headlineContent = { Text(deletedItem?.delItemName ?: "" ) },
        supportingContent = {
            Row {
                deletedItem?.delItemContentTypeStringResource?.also {
                    Text("${stringResource(MR.strings.type)}: ${stringResource(it)}")
                }

                Spacer(Modifier.width(8.dp))

                Text("${stringResource(MR.strings.deleted)}: $deletedTime")
            }
        },
        leadingContent = {
            if(deletedItem?.delItemIsFolder == true) {
                Icon(Icons.Default.Folder, contentDescription = null)
            }else {
                Icon(Icons.Default.InsertDriveFile, contentDescription = null)
            }
        },
        trailingContent = {
            Row {
                UstadTooltipBox(
                    tooltipText = stringResource(MR.strings.restore)
                ) {
                    IconButton(
                        onClick = {
                            deletedItem?.also(onClickRestore)
                        }
                    ) {
                        Icon(
                            Icons.Default.Restore,
                            contentDescription = stringResource(MR.strings.restore)
                        )
                    }
                }

                UstadTooltipBox(
                    tooltipText = stringResource(MR.strings.delete_permanently)
                ) {
                    IconButton(
                        onClick = {
                            deletedItem?.also(onClickDeletePermanently)
                        }
                    ) {
                        Icon(
                            Icons.Default.DeleteForever,
                            contentDescription = stringResource(MR.strings.delete_permanently)
                        )
                    }
                }
            }
        }
    )
}