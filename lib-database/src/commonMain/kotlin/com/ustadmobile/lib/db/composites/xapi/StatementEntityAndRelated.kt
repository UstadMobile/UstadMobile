package com.ustadmobile.lib.db.composites.xapi

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.xapi.ActorEntity
import com.ustadmobile.lib.db.entities.xapi.GroupMemberActorJoin
import com.ustadmobile.lib.db.entities.xapi.StatementEntity
import kotlinx.serialization.Serializable

@Serializable
class StatementEntityAndRelated(
    @Embedded
    var statementEntity: StatementEntity? = null,

    @Embedded
    var groupMemberActorJoin: GroupMemberActorJoin? = null,

    @Embedded
    var actorEntity: ActorEntity? = null,
)

