package com.ustadmobile.core.db.ext

import com.ustadmobile.door.DatabaseBuilder
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorSyncableDatabaseCallback2
import com.ustadmobile.door.entities.NodeIdAndAuth
import com.ustadmobile.door.ext.syncableTableIdMap

fun DatabaseBuilder<UmAppDatabase>.addSyncCallback(
    nodeIdAndAuth: NodeIdAndAuth,
    primary: Boolean
): DatabaseBuilder<UmAppDatabase> {
    addCallback(
        DoorSyncableDatabaseCallback2(nodeIdAndAuth.nodeId,
        com.ustadmobile.core.db.UmAppDatabase::class.syncableTableIdMap, primary)
    )

    return this
}