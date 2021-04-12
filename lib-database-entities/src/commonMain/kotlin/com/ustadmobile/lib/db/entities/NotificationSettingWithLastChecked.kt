package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

class NotificationSettingWithLastChecked : NotificationSetting(){

    @Embedded
    var notificationSetingLastChecked: NotificationSettingLastChecked? = null

}