package com.ustadmobile.core.io

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorUri

class ContainerBuilder internal constructor(
    internal val db: UmAppDatabase,
    internal val contentEntryUid: Long,
    internal val mimeType: String,
    internal val containerStorageUri: DoorUri,
){

    enum class Compression {
        NONE, GZIP
    }

    sealed class ContainerSource {

    }

    internal val containerSources = mutableListOf<ContainerSource>()


    companion object {

        val DEFAULT_DONT_COMPRESS_EXTENSIONS = arrayOf(
            "mp3", "ogg", "wav", "wma",
            "mp4", "mov", "wmv", "avi", "flv", "mkv", "webm",
            "pdf", "")

    }
}