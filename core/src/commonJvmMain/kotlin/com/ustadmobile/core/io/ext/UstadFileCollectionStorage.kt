package com.ustadmobile.core.io.ext

import com.turn.ttorrent.client.storage.FileCollectionStorage
import com.turn.ttorrent.client.storage.FileStorage
import com.turn.ttorrent.common.TorrentMetadata
import java.io.File
import java.io.IOException
import java.util.*

object UstadFileCollectionStorage {

    fun createFileCollectionStorage(metadata: TorrentMetadata, parent: File): FileCollectionStorage {
        if (!parent.isDirectory) {
            throw IllegalArgumentException("Invalid parent directory!");
        }
        val files = LinkedList<FileStorage>()
        var offset = 0L
        var totalSize = 0L
        metadata.files.forEach {
            val actual = File(parent, it.relativePathAsString)

            if(!actual.isParentOf(parent)){
                throw SecurityException("Torrent file path attempted to break directory jail!")
            }
            val canMakeDirectory = actual.parentFile?.mkdirs() ?: false
            if (actual.parentFile?.exists() == false && canMakeDirectory) {
                throw IOException("""Unable to create directories ${actual.parent} 
                    for storing torrent file ${actual.name}""");
            }
            files.add(FileStorage(actual, offset, it.size))
            offset += it.size
            totalSize += it.size
        }
        return FileCollectionStorage(files, totalSize);
    }

}