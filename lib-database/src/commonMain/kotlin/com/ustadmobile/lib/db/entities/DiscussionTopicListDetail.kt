package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class DiscussionTopicListDetail() : DiscussionTopic() {

    var numPosts: Int = 0

    var lastActiveTimestamp: Long = 0


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DiscussionTopicListDetail

        if (numPosts != other.numPosts) return false
        if (lastActiveTimestamp != other.lastActiveTimestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = numPosts
        result = 31 * result + lastActiveTimestamp.hashCode()
        return result
    }


}
