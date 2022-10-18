package com.ustadmobile.core.io

import com.ustadmobile.door.DoorUri

class ContainerBuilder internal constructor(){

    internal var containerStorageUri: DoorUri? = null

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