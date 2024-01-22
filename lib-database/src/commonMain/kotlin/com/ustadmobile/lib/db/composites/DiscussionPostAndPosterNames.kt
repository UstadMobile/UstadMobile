package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.DiscussionPost
import kotlinx.serialization.Serializable

@Serializable
data class DiscussionPostAndPosterNames(
    @Embedded
    var discussionPost: DiscussionPost? = null,
    var firstNames: String? = null,
    var lastName: String? = null,
    var personPictureUri: String? = null,
)
