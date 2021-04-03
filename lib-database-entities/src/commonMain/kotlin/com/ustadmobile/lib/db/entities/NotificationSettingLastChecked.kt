package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tracks when a notificationsetting was last checked on a given device.
@Entity
class NotificationSettingLastChecked {

    @PrimaryKey(autoGenerate = true)
    var nslcUid: Long = 0

    //foreignkey: notification setting uid
    var nslcNsUid: Long = 0

    var lastCheckTime: Long = 0

}