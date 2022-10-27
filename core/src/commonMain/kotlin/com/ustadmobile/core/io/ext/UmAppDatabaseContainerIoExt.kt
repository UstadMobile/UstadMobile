package com.ustadmobile.core.io.ext

import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ContainerBuilder
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.Container
import org.kodein.di.DI

expect suspend fun UmAppDatabase.addEntriesToContainerFromZip(
    containerUid: Long,
    zipUri: DoorUri,
    addOptions: ContainerAddOptions,
    context: Any
)

expect suspend fun UmAppDatabase.addDirToContainer(
    containerUid: Long,
    dirUri: DoorUri,
    recursive: Boolean = true,
    context: Any,
    di: DI,
    addOptions: ContainerAddOptions
)

expect suspend fun UmAppDatabase.addFileToContainer(
    containerUid: Long,
    fileUri: DoorUri,
    pathInContainer: String,
    context: Any,
    di: DI,
    addOptions: ContainerAddOptions
)


expect suspend fun ContainerBuilder.build(): Container


