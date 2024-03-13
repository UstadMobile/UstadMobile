package com.ustadmobile.libuicompose.view.discussionpost.detail

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ustadmobile.lib.db.composites.DiscussionPostAndPosterNames
import com.ustadmobile.libuicompose.components.UstadHtmlText
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.util.rememberDayOrDate
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import java.text.DateFormat
import com.ustadmobile.core.MR

@Composable
fun DiscussionPostListItem(
    discussionPost: DiscussionPostAndPosterNames?,
    showModerateOptions: Boolean = false,
    timeFormat: DateFormat,
    dateFormat: DateFormat,
    localDateTimeNow: LocalDateTime,
    dayOfWeekStringMap: Map<DayOfWeek, String>,
    onClickDelete: () -> Unit = { },
    modifier: Modifier = Modifier,
) {
    val dayOrDate = rememberDayOrDate(
        localDateTimeNow = localDateTimeNow,
        timestamp = discussionPost?.discussionPost?.discussionPostStartDate  ?: 0L,
        timeZone = kotlinx.datetime.TimeZone.currentSystemDefault(),
        showTimeIfToday = true,
        timeFormatter = timeFormat,
        dateFormatter = dateFormat,
        dayOfWeekStringMap = dayOfWeekStringMap,
    )

    val posterName = "${discussionPost?.firstNames ?: ""} ${discussionPost?.lastName ?: ""}"

    var menuExpanded by remember {
        mutableStateOf(false)
    }

    ListItem(
        modifier = modifier,
        leadingContent = {
            UstadPersonAvatar(
                personName = posterName,
                pictureUri = discussionPost?.personPictureUri
            )
        },
        headlineContent = {
            Text(
                text = posterName
            )
        },
        supportingContent = {
            UstadHtmlText(html = discussionPost?.discussionPost?.discussionPostMessage ?: "")
        },
        trailingContent = {
            Row {
                Text(dayOrDate)
                if(showModerateOptions) {
                    IconButton(
                        onClick = { menuExpanded = true }
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(MR.strings.more_options))
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                menuExpanded = false
                                onClickDelete()
                            },
                            text = { Text(stringResource(MR.strings.delete)) },
                        )
                    }
                }
            }

        }
    )
}