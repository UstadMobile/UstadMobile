package com.ustadmobile.lib.db.composites.xapi

import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import com.ustadmobile.lib.db.entities.xapi.VerbEntity
import kotlinx.serialization.Serializable

@Serializable
class StatementEntityAndVerb(
    var statementEntity: StatementEntity? = StatementEntity(),
    var verb: VerbEntity? = null,

)
