package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ContentEntryContentJobItemParams {

    var contentEntryUid: Long = 0L

    var leaf: Boolean = false

    var mostRecentContainerUid: Long = 0L

    var mostRecentContainerSize: Long = 0L

}