package com.ustadmobile.core.domain.export

import com.ustadmobile.core.db.dao.ContentEntryDao
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.rmi.server.ExportException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

abstract class CommonJvmExportContentEntryUstadZipUseCase(
    private val contentEntryDao: ContentEntryDao,
    private val json: Json
) : ExportContentEntryUstadZipUseCase {

    override suspend fun invoke(
        contentEntryUid: Long,
        destZipFilePath: String,
        progressListener: (ExportProgress) -> Unit
    ) {
        try {
            val entriesToExport = contentEntryDao.getRecursiveContentEntriesForExport(contentEntryUid)
            requireNotNull(entriesToExport.firstOrNull()) { "Content entry not found for UID: $contentEntryUid" }

            openOutputStream(destZipFilePath).use { zipOut ->
                entriesToExport.forEachIndexed { index, entry ->
                    val entryJson = json.encodeToString(entry)
                    val zipEntry = ZipEntry("${entry.contentEntryUid}.json")
                    zipOut.putNextEntry(zipEntry)
                    zipOut.write(entryJson.toByteArray())
                    zipOut.closeEntry()

                    val progress = (index + 1).toFloat() / entriesToExport.size
                    progressListener(ExportProgress(entry.title ?: "", entriesToExport.size, progress))
                }
            }

            progressListener(ExportProgress("Export completed", entriesToExport.size, 1f))
        } catch (e: Exception) {
            throw ExportException("Error during export: ${e.message}", e)
        }
    }

    protected abstract fun openOutputStream(destZipFilePath: String): ZipOutputStream
}