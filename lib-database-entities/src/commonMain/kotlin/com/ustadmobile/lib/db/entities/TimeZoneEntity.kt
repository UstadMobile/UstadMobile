package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class TimeZoneEntity(
    @PrimaryKey
    var id: String = "",

    var rawOffset: Int = 0
)