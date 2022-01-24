package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

class UserSessionAndPerson {

    @Embedded
    var person: Person? = null

    @Embedded
    var userSession: UserSession? = null

}