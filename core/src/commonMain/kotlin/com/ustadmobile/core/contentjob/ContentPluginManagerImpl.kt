package com.ustadmobile.core.contentjob

import com.ustadmobile.core.contentjob.*
import com.ustadmobile.door.DoorUri

class ContentPluginManager(val pluginList: List<ContentPlugin>) {

    fun getPluginById(id: Int): ContentPlugin {
        return pluginList.find { it.pluginId == id } ?: throw FatalContentJobException("invalid pluginId")
    }

    suspend fun extractMetadata(uri: DoorUri, processContext: ProcessContext): MetadataResult? {
        pluginList.forEach {
            try {
                return it.extractMetadata(uri, processContext) ?: return@forEach
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        return null
    }

}