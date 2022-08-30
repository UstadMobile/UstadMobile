package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
class VideoSubtitle() {

    @PrimaryKey(autoGenerate = true)
    var videoSubtitleUid: Long = 0

    /**
     * The MD5 of video subtitle contents (uncompressed), encoded with Base64 (trimmed with no trailing newline etc)
     */
    @ColumnInfo(index = true)
    var videoSubtitleMd5: String? = null

    /**
     * The path to the actual subtitle file
     */
    var videoSubtitlePath: String? = null

    /**
     * The total size of this subtitle uncompressed. This is important as it is sometimes used to to
     * provide Content-Length parameters
     */
    var videoSubtitleTotalSize: Long = 0

    /**
     * The size of this subtitle file (compressed). If the file is not compressed, then this would equal ceTotalSize
     */
    var videoSubtitleCompressedSize: Long = 0

    /**
     * The compression flag - COMPRESSION_NONE or COMPRESSION_GZIP
     */
    var videoSubtitlecompression: Int = 0

    var videoSubtitlelastModified: Long = 0

    /** Subtitle language code
     *
     */
    var videoSubtitleLanguage: String? = null

    companion object {

        const val COMPRESSION_NONE = 0

        const val COMPRESSION_GZIP = 1
    }
}
