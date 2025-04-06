package com.ustadmobile.lib.db.entities.respect

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @param razUid auto generated primary key
 * @param razRlUid foreign key join to respect lesson uid
 * @param razTitle assignment title
 * @param razDescription HTML text/description
 * @param razDeadline assignment deadline (ms since epoch)
 * @param razToClazzUid foreign key assignment to clazzUid
 */
@Entity
data class RespectAssignment(
    @PrimaryKey
    var razUid: Long = 0,

    var razToClazzUid: Long = 0,

    var razTitle: String = "",

    var razDescription: String = "",

    var razRlUid: Long = 0,

    var razDeadline: Long = 0,

    var razLastMod: Long = 0,
)
