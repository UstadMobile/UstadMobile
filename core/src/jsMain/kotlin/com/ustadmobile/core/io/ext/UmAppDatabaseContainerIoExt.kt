package com.ustadmobile.core.io.ext

import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ContainerBuilder
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.Container
import org.kodein.di.DI

actual suspend fun UmAppDatabase.addDirToContainer(
    containerUid: Long,
    dirUri: DoorUri,
    recursive: Boolean,
    context: Any, di: DI,
    addOptions: ContainerAddOptions) {
}

actual suspend fun UmAppDatabase.addFileToContainer(
    containerUid: Long,
    fileUri: DoorUri,
    pathInContainer: String,
    context: Any,
    di: DI,
    addOptions: ContainerAddOptions) {
}

actual suspend fun UmAppDatabase.addEntriesToContainerFromZip(
    containerUid: Long,
    zipUri: DoorUri,
    addOptions: ContainerAddOptions,
    context: Any) {
}

actual suspend fun ContainerBuilder.build(): Container {
    throw IllegalStateException("Not supported on Javascript")
}
