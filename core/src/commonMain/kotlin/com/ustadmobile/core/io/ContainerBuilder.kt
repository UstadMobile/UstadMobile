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


}