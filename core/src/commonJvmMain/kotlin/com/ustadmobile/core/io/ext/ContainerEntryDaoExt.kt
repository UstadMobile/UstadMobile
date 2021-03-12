package com.ustadmobile.core.io.ext

import com.ustadmobile.core.db.dao.ContainerEntryDao
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import java.io.*
import java.util.zip.GZIPInputStream

/**
 * Get an InputStream for a particular ContainerEntry. If the entry is compressed, the inputstream
 * will automatically inflate it as required.
 *
 * @param containerUid the ContainerUid to look in
 * @param pathInContainer the path within the container to open an input stream for
 */
fun ContainerEntryDao.openEntryInputStream(containerUid: Long, pathInContainer: String) : InputStream? {
    return findByPathInContainer(containerUid, pathInContainer)?.containerEntryFile?.let { entryFile ->
        entryFile.cefPath?.let { cefPath ->
            if(entryFile.compression == ContainerEntryFile.COMPRESSION_GZIP) {
                GZIPInputStream(FileInputStream(cefPath))
            }else {
                FileInputStream(cefPath)
            }
        }
    }
}
