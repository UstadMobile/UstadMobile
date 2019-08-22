package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
class DiscussionMessage() {

    @PrimaryKey
    var discussionMessageUid: Long = 0

    var posterPersonUid: Long = 0

    var message: String? = null

}
