package com.ustadmobile.lib.db.composites

import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import kotlinx.serialization.Serializable

@Serializable
data class PersonAndPictureAndNumAttempts(
    var person: Person = Person(),
    var picture: PersonPicture? = null,
    var numAttempts: Int = 0,
)
