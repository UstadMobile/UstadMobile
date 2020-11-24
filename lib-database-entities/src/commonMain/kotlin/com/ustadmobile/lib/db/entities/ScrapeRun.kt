package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
class ScrapeRun() {

    @PrimaryKey(autoGenerate = true)
    var scrapeRunUid: Int = 0

    var scrapeType: String? = null

    var scrapeRunStatus: Int = 0

    var conversionParams: String? = null

    constructor(scrapeType: String, status: Int, conversionParams: String?) : this() {
        this.scrapeType = scrapeType
        this.scrapeRunStatus = status
        this.conversionParams = conversionParams
    }
}
