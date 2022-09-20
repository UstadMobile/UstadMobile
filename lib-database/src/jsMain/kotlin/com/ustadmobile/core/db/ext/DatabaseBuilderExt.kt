package com.ustadmobile.core.db.ext

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.door.SyncNodeIdCallback
import com.ustadmobile.door.entities.NodeIdAndAuth

fun DatabaseBuilder<UmAppDatabase>.addSyncCallback(
    nodeIdAndAuth: NodeIdAndAuth
): DatabaseBuilder<UmAppDatabase> {
    addCallback(SyncNodeIdCallback(nodeIdAndAuth.nodeId))

    return this
}