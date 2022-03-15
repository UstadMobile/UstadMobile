package com.ustadmobile.core.util

import com.ustadmobile.door.DoorUri

actual fun createTemporaryDir(prefix: String): DoorUri {
    return DoorUri.parse(StorageUtil.createTempDir(prefix))
}