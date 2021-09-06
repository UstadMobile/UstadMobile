package com.ustadmobile.core.contentjob

import com.ustadmobile.door.DoorUri

interface ContentPluginManager {

    fun getPluginById(id: Int): ContentPlugin

    //go through plugins to
    suspend fun extractMetadata(uri: DoorUri) : MetadataResult?

}