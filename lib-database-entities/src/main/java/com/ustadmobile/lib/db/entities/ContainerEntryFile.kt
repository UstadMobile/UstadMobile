package com.ustadmobile.lib.db.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

@UmEntity
@Entity
class ContainerEntryFile {

    @UmPrimaryKey(autoIncrement = true)
    @PrimaryKey(autoGenerate = true)
    var cefUid: Long = 0

    var cefMd5: String? = null

    var cefPath: String? = null

    var ceTotalSize: Long = 0

    var ceCompressedSize: Long = 0

    var compression: Int = 0

    constructor()

    constructor(md5: String, totalSize: Long, compressedSize: Long, compression: Int) {
        this.cefMd5 = md5
        this.ceTotalSize = totalSize
        this.ceCompressedSize = compressedSize
        this.compression = compression
    }

    companion object {

        const val COMPRESSION_NONE = 0

        const val COMPRESSION_GZIP = 1
    }
}
