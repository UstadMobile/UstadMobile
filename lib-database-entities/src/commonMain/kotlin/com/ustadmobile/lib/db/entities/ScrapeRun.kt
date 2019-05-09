package com.ustadmobile.lib.db.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

@UmEntity
@Entity
class ScrapeRun {

    @UmPrimaryKey(autoIncrement = true)
    @PrimaryKey(autoGenerate = true)
    var scrapeRunUid: Int = 0

    var scrapeType: String? = null

    var status: Int = 0

    constructor()

    constructor(scrapeType: String, status: Int) {
        this.scrapeType = scrapeType
        this.status = status
    }
}
