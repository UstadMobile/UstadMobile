package com.ustadmobile.core.torrent

import bt.data.Storage
import bt.data.StorageUnit
import bt.data.file.OpenFileCache
import bt.metainfo.Torrent
import bt.metainfo.TorrentFile
import java.io.File
import java.nio.file.Path

class SharedFileSystemStorage(val rootDirectory: Path, val maxOpenFiles: Int = DEFAULT_MAX_OPEN_FILES) : Storage {

    private val cache = OpenFileCache(maxOpenFiles)

    /**
     * Create a file-system based storage inside a given directory.
     *
     * @param rootDirectory Root directory for this storage. All torrent files will be stored inside this directory.
     * @since 1.0
     */
    constructor(rootDirectory: File): this(rootDirectory.toPath())

    constructor(rootDirectory: Path): this(rootDirectory, DEFAULT_MAX_OPEN_FILES)


    override fun getUnit(torrent: Torrent, torrentFile: TorrentFile): StorageUnit {
        return SharedFileSystemStorageUnit(cache, rootDirectory,
                torrentFile.pathElements.joinToString(File.separator), 
                torrentFile.size)
    }

    override fun flush() {
        cache.flush()
    }

    companion object{
        const val DEFAULT_MAX_OPEN_FILES = 256
    }
}
