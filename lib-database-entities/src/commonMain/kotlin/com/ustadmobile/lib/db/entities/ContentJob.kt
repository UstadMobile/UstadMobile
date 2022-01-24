package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ContentJob(

        @PrimaryKey(autoGenerate = true)
        var cjUid: Long = 0,

        //Where data should be saved (null = default device storage)
        var toUri: String? = null,

        var cjProgress: Long = 0,

        var cjTotal: Long = 0,

        var cjNotificationTitle: String? = null,

        var cjIsMeteredAllowed: Boolean = false,

        var params: String? = null
)