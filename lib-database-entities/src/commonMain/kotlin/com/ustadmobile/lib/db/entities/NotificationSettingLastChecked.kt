package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tracks when a notificationsetting was last checked on a given device.
@Entity
class NotificationSettingLastChecked {

    //Primary key is the same as the NotificationSetting (allowing easy replacement)
    @PrimaryKey
    var nslcNsUid: Long = 0

    var lastCheckTime: Long = 0

}