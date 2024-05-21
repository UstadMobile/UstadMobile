package com.ustadmobile.core.domain.xapi.ext

import com.ustadmobile.core.db.dao.xapi.ActorDao
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.xapi.ActorEntity

suspend fun ActorDao.insertOrUpdateActorsIfNameChanged(actors: List<ActorEntity>) {
    insertOrIgnoreListAsync(actors)
    val timeNow = systemTimeInMillis()
    actors.forEach {
        updateIfNameChanged(
            uid = it.actorUid,
            name = it.actorName,
            updateTime = timeNow
        )
    }
}