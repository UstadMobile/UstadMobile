package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture

data class PersonAndListDisplayDetails(
    @Embedded
    var person: Person? = null,
    @Embedded
    var picture: PersonPicture? = null
)
