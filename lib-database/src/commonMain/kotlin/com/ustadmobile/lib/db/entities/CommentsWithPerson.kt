package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * POJO representing Comments and Person
 */
@Serializable
class CommentsWithPerson : Comments() {
    @Embedded
    var commentsPerson: Person ? = null


}
