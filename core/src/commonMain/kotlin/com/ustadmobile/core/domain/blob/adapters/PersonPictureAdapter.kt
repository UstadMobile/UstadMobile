package com.ustadmobile.core.domain.blob.adapters

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.BlobEntityAdapter
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis

class PersonPictureAdapter: BlobEntityAdapter {

    override suspend fun updateBlobUri(
        db: UmAppDatabase,
        updates: List<BlobEntityAdapter.BlobUpdate>
    ) {
        val time = systemTimeInMillis()
        db.withDoorTransactionAsync {
            updates.forEach { blob ->
                db.personPictureDao.updateUri(
                    uid = blob.uid,
                    uri = blob.uri,
                    time = time
                )
            }
        }
    }

    override suspend fun replicateUpstream(repo: UmAppDatabase, uids: List<Long>) {
        //Note - Content endpoint would no longer store content as the url... it would store
        // a mapping of /path/in/entry to bloburl e.g. https://endpoint.com/api/blob/sha256/content-type
        //(repo as DoorDatabaseRepository).clientState.
    }
}