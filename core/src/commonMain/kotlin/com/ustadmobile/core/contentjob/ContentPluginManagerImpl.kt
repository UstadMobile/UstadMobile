package com.ustadmobile.core.contentjob

import com.ustadmobile.core.contentjob.*
import com.ustadmobile.door.DoorUri

class ContentPluginManagerImpl(val pluginList: List<ContentPlugin>) : ContentPluginManager {

    override fun getPluginById(id: Int): ContentPlugin {
        return pluginList.find { it.pluginId == id } ?: throw FatalContentJobException("invalid pluginId")
    }

    override suspend fun extractMetadata(uri: DoorUri, processContext: ProcessContext): MetadataResult? {
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