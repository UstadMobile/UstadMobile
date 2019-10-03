package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class DailyActivityNumbers {

    var clazzUid: Long = 0
    var good: Int = 0
    var bad: Int = 0
    var dayDate: Long = 0
}
