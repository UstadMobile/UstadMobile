package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
class TimeZoneEntity(
    @PrimaryKey
    var id: String = "",

    var rawOffset: Int = 0
)