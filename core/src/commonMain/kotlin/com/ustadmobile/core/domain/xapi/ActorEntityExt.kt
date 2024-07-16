package com.ustadmobile.core.domain.xapi

import com.ustadmobile.lib.db.entities.xapi.ActorEntity

/**
 * Shorthand to differentiate between an anonymous (e.g. group) and an identified group
 * as per the xAPI spec.
 */
fun ActorEntity.isAnonymous(): Boolean {
    return (actorOpenid == null && actorMbox == null && actorAccountName == null && actorMbox_sha1sum == null)
}

