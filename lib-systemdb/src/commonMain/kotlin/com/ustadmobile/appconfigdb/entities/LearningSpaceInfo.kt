package com.ustadmobile.appconfigdb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @param lsiUid the XX64 hash of lsUrl
 * @param lsiUrl the full url e.g. https://subdomain.example.org/ . MUST end with a trailing slash
 */
@Entity
data class LearningSpaceInfo(
    @PrimaryKey
    var lsiUid: Long = 0,

    var lsiUrl: String = "",

    var lsiName: String = "",

    var lsiDescription: String = "",

    var lsiLastModified: Long = 0,

    var lsiStored: Long = 0,
)
