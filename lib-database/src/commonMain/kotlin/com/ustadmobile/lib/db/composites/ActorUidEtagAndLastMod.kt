package com.ustadmobile.lib.db.composites

data class ActorUidEtagAndLastMod(
    var actorUid: Long = 0,
    var actorEtag: Long = 0,
    var actorLct: Long = 0,
)
