package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import kotlinx.serialization.Serializable

@Serializable
data class PersonAndPictureAndNumAttempts(
    @Embedded
    var person: Person = Person(),
    @Embedded
    var picture: PersonPicture? = null,
    var numAttempts: Int = 0,
    var isCompleted: Boolean = false,
    var isSuccessful: Boolean? = null,
    var maxScore: Float? = null,
    var maxProgress: Int? = null,
)
