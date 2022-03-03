package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.ContainerEntryFileUidAndPath
import java.io.File

internal actual suspend fun deleteContainerEntryFilePaths(
    containerEntries: List<ContainerEntryFileUidAndPath>
): Pair<List<ContainerEntryFileUidAndPath>, List<ContainerEntryFileUidAndPath>> {
    return containerEntries.partition {
        it.cefPath?.let { cefPath -> File(cefPath) }?.also { file -> file.delete() }?.exists() == false
    }
}
