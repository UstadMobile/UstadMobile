package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
class ContainerEntryFile() {

    @PrimaryKey(autoGenerate = true)
    var cefUid: Long = 0

    /**
     * The MD5 of file contents (uncompressed), encoded with Base64 (trimmed with no trailing newline etc)
     */
    @ColumnInfo(index = true)
    var cefMd5: String? = null

    /**
     * The path to the actual file where the contents are stored (compressed if applicable)
     */
    var cefPath: String? = null

    /**
     * The total size of this file uncompressed. This is important as it is sometimes used to to
     * provide Content-Length parameters
     */
    var ceTotalSize: Long = 0

    /**
     * The size of this file (compressed). If the file is not compressed, then this would equal ceTotalSize
     */
    var ceCompressedSize: Long = 0

    /**
     * The compression flag - COMPRESSION_NONE or COMPRESSION_GZIP
     */
    var compression: Int = 0

    var lastModified: Long = 0

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

    constructor(md5: String, totalSize: Long, compressedSize: Long, compression: Int, lastModified: Long) : this() {
        this.cefMd5 = md5
        this.ceTotalSize = totalSize
        this.ceCompressedSize = compressedSize
        this.compression = compression
        this.lastModified = lastModified
    }

    companion object {

        const val COMPRESSION_NONE = 0

        const val COMPRESSION_GZIP = 1
    }
}
