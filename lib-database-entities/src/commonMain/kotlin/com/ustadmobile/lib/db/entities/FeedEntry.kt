package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class FeedEntry {

    @PrimaryKey(autoGenerate = true)
    var feUid: Long = 0

    @ColumnInfo(index = true)
    var fePersonUid: Long = 0

    var feTimestamp: Long = 0

    var feTitle: String? = null

    var feDescription: String? = null

    var feViewDest: String? = null

    var feEntityUid: Long = 0

    //Foreign Key: NotificationSetting uid
    var feNsUid: Long = 0
    
}