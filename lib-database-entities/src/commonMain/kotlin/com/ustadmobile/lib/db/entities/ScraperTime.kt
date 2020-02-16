package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
class ScraperTime {

    @PrimaryKey
    var timeUid: Long = 0

    var time: Long = 0
}