package com.ustadmobile.core.impl

import java.io.File

actual class ContainerStorageManager(
    storageDirs: List<File>
) {

    actual val storageList: List<ContainerStorageDir> = storageDirs.map {
        ContainerStorageDir(it.toURI().toString(), "Server space",
            it.usableSpace, false)
    }

}