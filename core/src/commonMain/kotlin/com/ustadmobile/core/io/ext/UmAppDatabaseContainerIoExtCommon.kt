package com.ustadmobile.core.io.ext

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ContainerBuilder
import com.ustadmobile.door.DoorUri

fun UmAppDatabase.containerBuilder(
    contentEntryUid: Long,
    mimeType: String,
    containerStorageUri: DoorUri
) = ContainerBuilder(this, contentEntryUid, mimeType, containerStorageUri)
