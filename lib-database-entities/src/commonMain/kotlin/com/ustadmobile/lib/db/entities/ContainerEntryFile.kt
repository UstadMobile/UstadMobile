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

    @ColumnInfo(index = true)
    var cefMd5: String? = null

    var cefPath: String? = null

    var ceTotalSize: Long = 0

    var ceCompressedSize: Long = 0

    var compression: Int = 0

    var lastModified: Long = 0

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
