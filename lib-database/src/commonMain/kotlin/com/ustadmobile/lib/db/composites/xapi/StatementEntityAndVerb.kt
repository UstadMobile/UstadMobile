package com.ustadmobile.lib.db.composites.xapi

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.xapi.ActivityEntity
import com.ustadmobile.lib.db.entities.xapi.ActivityLangMapEntry
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import com.ustadmobile.lib.db.entities.xapi.VerbEntity
import com.ustadmobile.lib.db.entities.xapi.VerbLangMapEntry
import kotlinx.serialization.Serializable

@Serializable
class StatementEntityAndVerb(
    @Embedded
    var statementEntity: StatementEntity = StatementEntity(),
    @Embedded
    var verb: VerbEntity? = null,
    @Embedded
    var verbDisplay: VerbLangMapEntry? = null,
    @Embedded
    var activity: ActivityEntity? = null,
    @Embedded
    var activityLangMapEntry: ActivityLangMapEntry? = null,

    var statementActivityDescription: String? = null,
)

object StatementConst{
    const val SORT_BY_TIMESTAMP_DESC = 1

    const val SORT_BY_TIMESTAMP_ASC = 2

    const val SORT_BY_SCORE_DESC = 3

    const val SORT_BY_SCORE_ASC = 4

}