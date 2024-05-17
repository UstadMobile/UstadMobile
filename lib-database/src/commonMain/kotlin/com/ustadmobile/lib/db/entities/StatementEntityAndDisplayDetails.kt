package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
data class StatementEntityAndDisplayDetails(

    @Embedded
    var statement: StatementEntity? = null,

    @Embedded
    var person: Person? = null,

    @Embedded
    var xlangMapEntry: XLangMapEntry? = null
)
