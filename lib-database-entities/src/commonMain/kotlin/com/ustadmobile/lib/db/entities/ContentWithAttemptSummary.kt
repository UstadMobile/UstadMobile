package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ContentWithAttemptSummary {

    var contentEntryUid: Long = 0

    var contentEntryTitle: String? = null

    var contentEntryThumbnailUrl: String? = null

    var attempts: Int = 0

    var startDate: Long = 0L

    var endDate: Long = Long.MAX_VALUE

    var duration: Long = 0L

    @Embedded
    var scoreProgress: ContentEntryStatementScoreProgress? = null

}