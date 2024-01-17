package com.ustadmobile.core.contentformats

import com.ustadmobile.core.contentjob.FatalContentJobException
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.door.DoorUri
import io.github.aakira.napier.Napier
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

    fun supportedFormatNames(): List<String> = importersList.map { it.formatName }

    suspend fun extractMetadata(
        uri: DoorUri,
        originalFilename: String? = null,
    ): MetadataResult? {
        importersList.forEach {
            try {
                return it.extractMetadata(uri, originalFilename) ?: return@forEach
            }catch(e: CancellationException) {
                throw e
            }catch(e: InvalidContentException) {
                throw e
            }catch(e: Throwable) {
                Napier.w("ExtractMetadata: Exception checking $uri using importer #${it.importerId}: $e")
            }
        }

        return null
    }

}