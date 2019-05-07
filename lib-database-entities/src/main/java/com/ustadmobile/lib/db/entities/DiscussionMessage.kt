package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

@UmEntity
class DiscussionMessage : SyncableEntity {

    @UmPrimaryKey
    var discussionMessageUid: Long = 0

    var posterPersonUid: Long = 0

    var message: String? = null

    override var masterChangeSeqNum: Long = 0

    override var localChangeSeqNum: Long = 0
}
