package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

/**
 * POJO representing Post and details for its display in a list screeen
 */
@Serializable
class DiscussionPostWithDetails : DiscussionPost() {

    var authorPersonFirstNames: String? = null

    var authorPersonLastName: String? = null

    var authorPictureUri: String? = null

    var postLatestMessage: String? = null

    var postRepliesCount: Int = 0

    var postLatestMessageTimestamp: Long = 0


}
