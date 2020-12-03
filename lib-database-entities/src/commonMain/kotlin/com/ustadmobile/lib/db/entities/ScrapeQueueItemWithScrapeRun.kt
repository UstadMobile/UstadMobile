package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ScrapeQueueItemWithScrapeRun : ScrapeQueueItem() {

    @Embedded
    var scrapeRun: ScrapeRun? = null

}