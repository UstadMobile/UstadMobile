package com.ustadmobile.libuicompose.view.message.conversationlist

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.ustadmobile.core.viewmodel.message.conversationlist.ConversationListUiState
import com.ustadmobile.lib.db.composites.MessageAndOtherPerson
import com.ustadmobile.libuicompose.components.UstadPersonAvatar
import com.ustadmobile.libuicompose.util.rememberDayOrDate
import kotlinx.datetime.TimeZone
import java.text.DateFormat


@Composable
fun ConversationListItem(
    message: MessageAndOtherPerson?,
    uiState: ConversationListUiState,
    timeFormatter: DateFormat,
    dateFormatter: DateFormat,
    onListItemClick: (MessageAndOtherPerson) -> Unit,
){

    val formattedDayOrDate = rememberDayOrDate(
        localDateTimeNow = uiState.localDateTimeNow,
        timestamp = message?.message?.messageTimestamp ?: 0,
        timeZone = TimeZone.currentSystemDefault(),
        showTimeIfToday = true,
        timeFormatter = timeFormatter,
        dateFormatter = dateFormatter,
        dayOfWeekStringMap = uiState.dayOfWeekStrings,
    )

    ListItem(
        modifier = Modifier.clickable {
            message?.also { onListItemClick(it) }
        },
        headlineContent = { Text(text = "${message?.otherPerson?.fullName()}") },
        leadingContent = {
            UstadPersonAvatar(
                pictureUri = message?.personPicture?.personPictureThumbnailUri,
                personName = message?.otherPerson?.fullName(),
            )
        },
        supportingContent = {
            Text(
                text = "${message?.message?.messageText}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingContent = { Text(formattedDayOrDate) }
    )
}