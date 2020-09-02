package com.ustadmobile.door.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class UpdateNotification(
        @PrimaryKey
        var pnUid: Long = 0,
        var pnDeviceId: Int = 0,
        var pnTableId: Int = 0,
        var pnTimestamp: Long = 0)