package com.ustadmobile.core.db.ext

import com.ustadmobile.core.contentformats.pdf.AbstractPdfContentImportCommonJvm
import com.ustadmobile.core.contentformats.video.VideoContentImporterCommonJvm
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.contententry.importcontent.ImportContentEntryUseCase
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import io.github.aakira.napier.Napier
import java.io.File

suspend fun UmAppDatabase.MigrateContainerToContentEntryVersion(
    importUseCase: ImportContentEntryUseCase,
) {
    val remainingContainersBatch = mutableListOf<Container>()
    while(remainingContainersBatch.also { it.addAll(containerDao.findAllBatch()) }.isNotEmpty() ) {
        println("Migrating ${remainingContainersBatch.size} containers...")

        remainingContainersBatch.forEach { container ->
            try {
                val containerEntries = containerEntryDao
                    .findByContainer(container.containerUid)

                fun createSingleFileJob(importerId: Int): ContentEntryImportJob {
                    val file = containerEntries.first().containerEntryFile
                    return ContentEntryImportJob(
                        sourceUri = File(file?.cefPath!!).toDoorUri().toString(),
                        cjiOriginalFilename = containerEntries.first().cePath,
                        cjiContentEntryUid = container.containerContentEntryUid,
                        cjiPluginId = importerId,
                    )
                }

                when(container.mimeType) {
                    "video/mp4" ->  {
                        val jobItem = createSingleFileJob(
                            VideoContentImporterCommonJvm.IMPORTER_ID)
                        val jobUid = contentEntryImportJobDao.insertJobItem(jobItem)
                        importUseCase(jobUid)
                    }
                    "application/pdf" -> {
                        val jobItem = createSingleFileJob(
                            AbstractPdfContentImportCommonJvm.PLUGINID)
                        val jobUid = contentEntryImportJobDao.insertJobItem(jobItem)
                        importUseCase(jobUid)
                    }
                }
            }catch(e: Throwable) {
                Napier.e("Exception importing ${container.containerUid}", e)
            }
        }

        containerDao.deleteListAsync(remainingContainersBatch)
        remainingContainersBatch.clear()
    }
}