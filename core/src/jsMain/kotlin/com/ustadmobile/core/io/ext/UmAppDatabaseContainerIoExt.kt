package com.ustadmobile.core.io.ext

import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorUri

actual suspend fun UmAppDatabase.addDirToContainer(
    containerUid: Long,
    dirUri: DoorUri,
    recursive: Boolean,
    addOptions: ContainerAddOptions) {
}

actual suspend fun UmAppDatabase.addFileToContainer(
    containerUid: Long,
    fileUri: DoorUri,
    pathInContainer: String,
    addOptions: ContainerAddOptions) {
}

actual suspend fun UmAppDatabase.addEntriesToContainerFromZip(
    containerUid: Long,
    zipUri: DoorUri,
    addOptions: ContainerAddOptions,
    context: Any) {
}