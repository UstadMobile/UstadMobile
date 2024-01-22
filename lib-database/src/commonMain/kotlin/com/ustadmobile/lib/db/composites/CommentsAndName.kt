package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.Comments
import kotlinx.serialization.Serializable

@Serializable
data class CommentsAndName(
    @Embedded
    var comment: Comments = Comments(),
    var firstNames: String? = null,
    var lastName: String? = null,
    var pictureUri: String? = null,
)
