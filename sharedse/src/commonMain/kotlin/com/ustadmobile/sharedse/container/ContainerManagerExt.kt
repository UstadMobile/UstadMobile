package com.ustadmobile.sharedse.container

import com.github.aakira.napier.Napier
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.ContainerManagerCommon
import com.ustadmobile.core.util.ext.base64StringToByteArray
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import com.ustadmobile.sharedse.io.ConcatenatedInputStream
import com.ustadmobile.sharedse.io.ConcatenatedInputStreamEntrySource

/**
 * Add the contents of a ConcatenatedInputStream to the given ContainerManager.
 *
 * @param concatenatedInputStream ConcatenatedInputStream from which to read new entries
 * @param entries A list of ContainerEntryWithMd5s that correspond to the contents that are in the
 * concatenatedInputStream. This is used to create ContainerEntry objects.
 */
suspend fun ContainerManager.addEntriesFromConcatenatedInputStream(
        concatenatedInputStream: ConcatenatedInputStream, entries: List<ContainerEntryWithMd5>) {

    val pathToMd5Map = entries.map {
        (it.cePath ?: "") to (it.cefMd5?.base64StringToByteArray() ?: ByteArray(0))
    }.toMap()

    var entryCount = 0
    addEntries(ContainerManagerCommon.AddEntryOptions(dontUpdateTotals = true),
            pathToMd5Map) {

        val nextPart = concatenatedInputStream.nextPart()
        if (nextPart != null) {
            val partMd5Str = nextPart.id.encodeBase64()

            val pathsInContainer = entries.filter {
                it.cefMd5 == partMd5Str && it.cePath != null
            }

            if (pathsInContainer.isNotEmpty()) {
                entryCount++
                ConcatenatedInputStreamEntrySource(nextPart, concatenatedInputStream,
                        pathsInContainer.map { it.cePath!! })
            } else {
                Napier.wtf({ "Could not find path for md5sum $partMd5Str" })
                throw IllegalStateException("Could not find the path of md5sum $partMd5Str")
            }
        } else {
            null
        }

    }
}
