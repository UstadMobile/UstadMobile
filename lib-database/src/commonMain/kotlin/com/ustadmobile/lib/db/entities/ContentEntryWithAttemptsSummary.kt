package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ContentEntryWithAttemptsSummary {

    var contentEntryUid: Long = 0

    var title: String? = null

    var thumbnailUrl: String? = null

    var attempts: Int = 0

    var progress: Int = 0

    var startDate: Long = 0L

    var endDate: Long = Long.MAX_VALUE

    var duration: Long = 0L

    var resultScoreScaled: Float = 0f

    var resultMax: Int = 0

    var resultScore: Int = 0


}