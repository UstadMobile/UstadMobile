package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import com.ustadmobile.lib.db.entities.xapi.VerbEntity
import kotlinx.serialization.Serializable

@Serializable
data class StatementAndSessionDetailDisplay(
    @Embedded
    var statement: StatementEntity? = null,

    @Embedded
    var verb: VerbEntity? = null,

    var verbDisplay: String?= null,

    var objectDisplay: String? = null,
)
