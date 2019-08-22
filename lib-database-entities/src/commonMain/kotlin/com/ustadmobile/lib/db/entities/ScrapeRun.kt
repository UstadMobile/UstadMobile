package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ScrapeRun() {

    @PrimaryKey(autoGenerate = true)
    var scrapeRunUid: Int = 0

    var scrapeType: String? = null

    var status: Int = 0

    constructor(scrapeType: String, status: Int) : this() {
        this.scrapeType = scrapeType
        this.status = status
    }
}
