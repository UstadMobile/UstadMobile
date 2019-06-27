package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

@UmEntity
@Entity
class ScrapeQueueItem() {

    @UmPrimaryKey(autoIncrement = true)
    @PrimaryKey(autoGenerate = true)
    var sqiUid: Int = 0

    var sqiContentEntryParentUid: Long = 0

    var destDir: String? = null

    var scrapeUrl: String? = null

    var status: Int = 0

    var runId: Int = 0

    var itemType: Int = 0

    var contentType: String? = null

    var timeAdded: Long = 0

    var timeStarted: Long = 0

    var timeFinished: Long = 0

    companion object {

        const val ITEM_TYPE_INDEX = 1

        const val ITEM_TYPE_SCRAPE = 2
    }
}
