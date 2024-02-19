package com.ustadmobile.libuicompose.view.message.messagelist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ustadmobile.core.viewmodel.message.daysUntil
import com.ustadmobile.core.viewmodel.message.messagelist.MessageListUiState
import com.ustadmobile.lib.db.entities.Message
import com.ustadmobile.libuicompose.components.UstadLinkifyText
import com.ustadmobile.libuicompose.util.linkify.ILinkExtractor
import com.ustadmobile.libuicompose.util.rememberDayOrDate
import kotlinx.datetime.TimeZone
import java.text.DateFormat
import java.util.Date


@Composable
fun MessageListItem(
    message: Message?,
    previousMessage: Message? = null,
    activeUserUid: Long,
    linkExtractor: ILinkExtractor,
    uiState: MessageListUiState,
    timeFormatter: DateFormat,
    dateFormatter: DateFormat,
    modifier: Modifier = Modifier,
) {
    val isFromMe = message?.messageSenderPersonUid == activeUserUid
    val daysSincePrevMessage = remember(message?.messageTimestamp, previousMessage?.messageTimestamp) {
        message?.let { previousMessage?.daysUntil(it) }
    }

    Column(
        modifier = modifier,
    ) {
        if(daysSincePrevMessage != 0) {
            val header = rememberDayOrDate(
                localDateTimeNow = uiState.localDateTimeNow,
                timestamp = message?.messageTimestamp ?: 0,
                timeZone = TimeZone.currentSystemDefault(),
                showTimeIfToday = false,
                timeFormatter = timeFormatter,
                dayOfWeekStringMap = uiState.dayOfWeekStrings,
                dateFormatter = dateFormatter,
            )

            Text(
                text = header,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }

        Box(
            modifier = Modifier
                .align(if (isFromMe) Alignment.End else Alignment.Start)
                .clip(
                    RoundedCornerShape(
                        topStart = 16f,
                        topEnd = 16f,
                        bottomStart = if (isFromMe) 16f else 0f,
                        bottomEnd = if (isFromMe) 0f else 16f
                    )
                )
                .background(
                    if(isFromMe) {
                        MaterialTheme.colorScheme.primaryContainer
                    }else {
                        MaterialTheme.colorScheme.tertiaryContainer
                    }
                )
                .padding(8.dp)
        ) {
            Column {
                UstadLinkifyText(
                    text = message?.messageText ?: "",
                    linkExtractor = linkExtractor,
                )
                Text(
                    modifier = Modifier,
                    style = MaterialTheme.typography.labelSmall,
                    text = remember(message?.messageTimestamp ?: 0L) {
                        timeFormatter.format(Date(message?.messageTimestamp ?: 0L))
                    },
                    textAlign = TextAlign.End,
                )
            }

        }
    }
}