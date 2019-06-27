package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

@UmEntity
@Entity
class ScrapeRun() {

    @UmPrimaryKey(autoIncrement = true)
    @PrimaryKey(autoGenerate = true)
    var scrapeRunUid: Int = 0

    var scrapeType: String? = null

    var status: Int = 0

    constructor(scrapeType: String, status: Int) : this() {
        this.scrapeType = scrapeType
        this.status = status
    }
}
