package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

data class UserSessionAndPerson(
    @Embedded
    var person: Person? = null,

    @Embedded
    var personPicture: PersonPicture? = null,

    @Embedded
    var userSession: UserSession? = null,

)
