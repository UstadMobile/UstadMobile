package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import kotlinx.serialization.Serializable

@Serializable
data class StatementEntityAndDisplayDetails(

    @Embedded
    var statement: StatementEntity? = null,

    @Embedded
    var person: Person? = null,

)
