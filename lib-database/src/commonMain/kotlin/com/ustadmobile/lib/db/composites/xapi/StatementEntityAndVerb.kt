package com.ustadmobile.lib.db.composites.xapi

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import com.ustadmobile.lib.db.entities.xapi.VerbEntity
import com.ustadmobile.lib.db.entities.xapi.VerbLangMapEntry
import kotlinx.serialization.Serializable

@Serializable
class StatementEntityAndVerb(
    @Embedded
    var statementEntity: StatementEntity? = StatementEntity(),
    @Embedded
    var verb: VerbEntity? = null,
    @Embedded
    var verbDisplay: VerbLangMapEntry? = null,
)
