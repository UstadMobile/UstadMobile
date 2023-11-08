package com.ustadmobile.core.contentjob

import com.ustadmobile.door.DoorUri
import kotlinx.coroutines.CancellationException

class ContentImportersManager(
    private val importersList: List<ContentImporter>
) {

    init{
        val duplicateImporters = importersList.filter { plugin ->
            importersList.count { it.importerId == plugin.importerId } > 1
        }

        if(duplicateImporters.isNotEmpty()) {
            throw IllegalArgumentException("Duplicate ContentImporter importerIds in: ${duplicateImporters.joinToString()}")
        }
    }

    fun requireImporterById(id: Int) : ContentImporter {
        return importersList.find { it.importerId == id } ?: throw FatalContentJobException("invalid pluginId")
    }

    fun getImporterById(id: Int): ContentImporter? {
        return importersList.firstOrNull { it.importerId == id }
    }

    suspend fun extractMetadata(
        uri: DoorUri,
        originalFilename: String? = null,
    ): MetadataResult {
        importersList.forEach {
            try {
                return it.extractMetadata(uri, originalFilename) ?: return@forEach
            }catch (e: Exception){
                if(e is CancellationException){
                    throw e
                }
                e.printStackTrace()
            }
        }

        throw ContentTypeNotSupportedException("no content importer found for $uri")
    }

}