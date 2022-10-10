package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
class DeviceSession(
    @PrimaryKey(autoGenerate = true)
    var deviceSessionUid: Long = 0,
    var dsDeviceId: Int = 0,
    var dsPersonUid: Long = 0,
    var expires: Long = 0
)