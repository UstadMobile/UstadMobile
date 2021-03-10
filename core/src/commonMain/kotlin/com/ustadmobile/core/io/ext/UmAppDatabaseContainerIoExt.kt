package com.ustadmobile.core.io.ext

import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.db.UmAppDatabase

expect suspend fun UmAppDatabase.addEntriesToContainerFromZip(containerUid: Long,
                                                              zipUri: String,
                                                              addOptions: ContainerAddOptions)

expect suspend fun UmAppDatabase.addDirToContainer(containerUid: Long, dirUri: String,
                                                   recursive: Boolean = true,
                                                   addOptions: ContainerAddOptions)

