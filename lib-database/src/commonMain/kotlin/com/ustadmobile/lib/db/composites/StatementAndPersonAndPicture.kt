package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonPicture
import com.ustadmobile.lib.db.entities.xapi.StatementEntity

data class StatementAndPersonAndPicture(
    @Embedded
    var person: Person? = null,
    @Embedded
    var statement: StatementEntity? = null,
    @Embedded
    var picture: PersonPicture? = null,

    var numberOfAttempts:Int=0

)