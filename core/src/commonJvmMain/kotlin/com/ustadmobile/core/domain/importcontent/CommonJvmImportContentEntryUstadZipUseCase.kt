package com.ustadmobile.core.domain.import

import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.serialization.json.Json
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import com.ustadmobile.core.db.dao.ContentEntryDao

abstract class CommonJvmImportContentEntryUstadZipUseCase(
    private val contentEntryDao: ContentEntryDao,
    private val json: Json
) : ImportContentEntryUstadZipUseCase {

    override suspend fun invoke(
        sourceZipFilePath: String,
        progressListener: (ImportProgress) -> Unit
    ) {
        try {
            openInputStream(sourceZipFilePath).use { zipIn ->
                var entry: ZipEntry? = zipIn.nextEntry
                var count = 0

                while (entry != null) {
                    if (!entry.isDirectory) {
                        val jsonBytes = zipIn.readBytes()
                        val entryJson = json.decodeFromString<ContentEntry>(jsonBytes.decodeToString())
                        contentEntryDao.insert(entryJson)

                        count++
                        // Calculate progress based on count
                        val progress = count.toFloat() / estimateTotalEntries(sourceZipFilePath).toFloat()
                        progressListener(ImportProgress(entry.name, count, progress))
                    }
                    entry = zipIn.nextEntry
                }
            }
            progressListener(ImportProgress("Import completed", 1, 1f))
        } catch (e: Exception) {
            throw Exception("Error during import: ${e.message}", e)
        }
    }

    protected abstract fun openInputStream(sourceZipFilePath: String): ZipInputStream

    private fun estimateTotalEntries(sourceZipFilePath: String): Int {
        // Implement your logic to estimate total entries if necessary
        return 0 // Placeholder; adjust as per your actual logic
    }
}
