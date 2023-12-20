package com.ustadmobile.core.domain.saveblob

import com.ustadmobile.core.db.UmAppDatabase

/**
 * Implement this adapter for each type of image entity. Then one job can lookup the adapter by
 * table id.
 */
interface BlobEntityAdapter {

    class BlobUpdate(
        val uid: Long,
        val uri: String?
    )

    suspend fun updateBlobUri(
        db: UmAppDatabase,
        updates: List<BlobUpdate>,
    )

    suspend fun replicateUpstream(
        repo: UmAppDatabase,
        uids: List<Long>,
    )

}