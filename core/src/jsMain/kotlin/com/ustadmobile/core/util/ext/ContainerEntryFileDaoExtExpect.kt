package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.entities.ContainerEntryFileUidAndPath

/**
 * Delete the actual files given by a list of containerentryfileuidandpaths
 *
 * @return pair of two lists. First list is those successfully deleted, the second list is those
 * that were not deleted
 */
internal actual suspend fun deleteContainerEntryFilePaths(containerEntries: List<ContainerEntryFileUidAndPath>): Pair<List<ContainerEntryFileUidAndPath>, List<ContainerEntryFileUidAndPath>> {
    TODO("Not yet implemented")
}