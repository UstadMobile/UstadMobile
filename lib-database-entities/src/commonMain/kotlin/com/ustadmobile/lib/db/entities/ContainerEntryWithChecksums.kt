package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo

class ContainerEntryWithChecksums: ContainerEntry() {

    /**
     * The MD5 of file contents (uncompressed), encoded with Base64 (trimmed with no trailing newline etc)
     */
    @ColumnInfo(index = true)
    var cefMd5: String? = null

    /**
     * The Sub Resource Integrity (SRI) String e.g. sha256-base64 . If this data is compressed,
     * this is the sha256 of the compressed data
     */
    var cefIntegrity: String? = null

    /**
     * The CRC of the compressed data (if this data is compressed, this is the crc of the compressed
     * data)
     */
    var cefCrc32: Long = 0

    /**
     * The size of this file (compressed). If the file is not compressed, then this would equal ceTotalSize
     */
    var ceCompressedSize: Long = 0

}