package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class PersonWithAttemptsSummary {

    var personUid: Long = 0

    var firstNames: String? = null

    var lastName: String? = null

    var attempts: Int = 0

    var startDate: Long = 0L

    var endDate: Long = Long.MAX_VALUE

    var duration: Long = 0L

    var latestPrivateComment: String? = null

    @Embedded
    var scoreProgress: ContentEntryStatementScoreProgress? = null


}