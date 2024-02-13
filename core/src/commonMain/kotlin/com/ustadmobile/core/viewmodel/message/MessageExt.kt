package com.ustadmobile.core.viewmodel.message

import com.ustadmobile.lib.db.entities.Message
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil

/**
 * Days until the timestamp of the other message
 */
fun Message.daysUntil(other: Message): Int {
    return Instant.fromEpochMilliseconds(this.messageTimestamp)
            .daysUntil(
                other = Instant.fromEpochMilliseconds(other.messageTimestamp),
                timeZone = TimeZone.currentSystemDefault()
            )
}