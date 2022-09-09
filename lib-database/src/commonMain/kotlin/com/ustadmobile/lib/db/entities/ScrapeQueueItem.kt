package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(indices = [Index(value = ["status","itemType"])])
@Serializable
open class ScrapeQueueItem() {

    @PrimaryKey(autoGenerate = true)
    var sqiUid: Int = 0

    var sqiContentEntryParentUid: Long = 0

    var sqiContentEntryUid: Long = 0

    var destDir: String? = null

    var scrapeUrl: String? = null

    var status: Int = 0

    var runId: Int = 0

    var itemType: Int = 0

    var errorCode: Int = 0

    var contentType: String? = null

    var timeAdded: Long = 0

    var timeStarted: Long = 0

    var timeFinished: Long = 0

    var priority: Int = 0

    var overrideEntry: Boolean = false

    companion object {

        const val ITEM_TYPE_INDEX = 1

        const val ITEM_TYPE_SCRAPE = 2
    }
}
