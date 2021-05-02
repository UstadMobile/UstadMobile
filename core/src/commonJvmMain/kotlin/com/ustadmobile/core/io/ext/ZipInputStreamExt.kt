package com.ustadmobile.core.io.ext

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * Skip the given ZipInputStream to entry that matches criteria. Return the entry if found, otherwise
 * return null
 *
 * @param block checker function
 */
fun ZipInputStream.skipToEntry(block: (ZipEntry) -> Boolean): ZipEntry? {
    lateinit var entry: ZipEntry
    while(nextEntry?.also { entry = it } != null) {
        if(block(entry))
            return entry
    }

    return null
}
