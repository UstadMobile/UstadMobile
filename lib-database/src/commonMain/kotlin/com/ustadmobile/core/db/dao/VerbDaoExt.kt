package com.ustadmobile.core.db.dao

import com.ustadmobile.lib.db.entities.VerbEntity

suspend fun VerbDao.initPreloadedVerbs() {
    val uidsInserted = findByUidList(VerbEntity.FIXED_UIDS.values.toList())
    val uidsToInsert = VerbEntity.FIXED_UIDS.filter { it.value !in uidsInserted }
    val verbListToInsert = uidsToInsert.map { verbEntry ->
        VerbEntity(verbEntry.value, verbEntry.key)
    }
    replaceList(verbListToInsert)
}