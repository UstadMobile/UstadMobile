package com.ustadmobile.core.domain.xapi.ext

import com.ustadmobile.core.db.dao.xapi.GroupMemberActorJoinDao
import com.ustadmobile.lib.db.entities.xapi.GroupMemberActorJoin

/**
 * Ensure that group member joins match the last modified time.
 */
suspend fun GroupMemberActorJoinDao.insertOrUpdateIfLastModChanged(
    memberJoins: List<GroupMemberActorJoin>,
    lastModTime: Long,
) {
    insertOrIgnoreListAsync(memberJoins)
    memberJoins.forEach {
        updateLastModifiedTimeIfNeededAsync(
            gmajGroupActorUid = it.gmajGroupActorUid,
            gmajMemberActorUid = it.gmajMemberActorUid,
            lastModTime = lastModTime,
        )
    }
}
