package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

@kotlinx.serialization.Serializable
class DiscussionPostWithPerson: DiscussionPost() {
    @Embedded
    var replyPerson: Person? = null


}