package com.ustadmobile.lib.db.composites.xapi

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.xapi.ActivityEntity
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import kotlinx.serialization.Serializable

@Serializable
data class StatementAndActivity(
    @Embedded
    var statementEntity: StatementEntity = StatementEntity(),
    @Embedded
    var activityEntity: ActivityEntity? = null
)