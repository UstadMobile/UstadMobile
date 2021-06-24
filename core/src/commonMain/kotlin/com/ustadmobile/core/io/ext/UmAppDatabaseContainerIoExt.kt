package com.ustadmobile.core.io.ext

import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorUri

expect suspend fun UmAppDatabase.addEntriesToContainerFromZip(containerUid: Long,
                                                              zipUri: DoorUri,
                                                              addOptions: ContainerAddOptions,
                                                              context: Any)

expect suspend fun UmAppDatabase.addDirToContainer(containerUid: Long, dirUri: DoorUri,
                                                   recursive: Boolean = true,
                                                   addOptions: ContainerAddOptions)

expect suspend fun UmAppDatabase.addFileToContainer(containerUid: Long, fileUri: DoorUri,
                                                    pathInContainer: String, addOptions: ContainerAddOptions)

