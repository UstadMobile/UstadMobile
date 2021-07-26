package com.ustadmobile.mocks.db

import com.ustadmobile.core.db.dao.ScopedGrantDao
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.lib.db.entities.ScopedGrantAndName

class ScopedGrantDaoJs: ScopedGrantDao() {

    override suspend fun insertAsync(scopedGrant: ScopedGrant): Long {
        TODO("Not yet implemented")
    }

    override suspend fun insertListAsync(scopedGrantList: List<ScopedGrant>) {
        TODO("Not yet implemented")
    }

    override suspend fun updateAsync(scopedGrant: ScopedGrant) {
        TODO("Not yet implemented")
    }

    override suspend fun updateListAsync(scopedGrantList: List<ScopedGrant>) {
        TODO("Not yet implemented")
    }

    override suspend fun findByTableIdAndEntityUid(
        tableId: Int,
        entityUid: Long
    ): List<ScopedGrantAndName> {
        return listOf()
    }
}