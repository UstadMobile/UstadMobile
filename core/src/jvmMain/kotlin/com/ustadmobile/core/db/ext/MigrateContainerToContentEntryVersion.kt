package com.ustadmobile.core.db.ext

import com.ustadmobile.core.contentformats.ContentImportersManager
import com.ustadmobile.core.contentformats.pdf.AbstractPdfContentImportCommonJvm
import com.ustadmobile.core.contentformats.video.VideoContentImporterCommonJvm
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.contententry.importcontent.ImportContentEntryUseCase
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import io.github.aakira.napier.Napier
import java.io.File
import java.io.FileInputStream
import java.util.zip.GZIPInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

suspend fun UmAppDatabase.MigrateContainerToContentEntryVersion(
    importUseCase: ImportContentEntryUseCase,
    importersManager: ContentImportersManager,
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
                    else -> {
                        //it is zip content
                        val extension = when(container.mimeType) {
                            "application/epub+zip" -> "epub"
                            else -> "zip"
                        }
                        val filename = "container-convert-${container.containerUid}.$extension"

                        val tmpZipFile = File.createTempFile("container-${container.containerUid}", ".$extension")
                        ZipOutputStream(tmpZipFile.outputStream()).use { zipOut->
                            containerEntries.forEach { containerEntry ->
                                val name = containerEntry.cePath
                                val filePath = containerEntry.containerEntryFile?.cefPath
                                val compression = containerEntry.containerEntryFile?.compression
                                    ?: ContainerEntryFile.COMPRESSION_NONE
                                if(name != null && filePath != null) {
                                    zipOut.putNextEntry(ZipEntry(name))
                                    FileInputStream(filePath).use { fileInStream ->
                                        val entryInStream = if(
                                            compression == ContainerEntryFile.COMPRESSION_GZIP
                                        ) {
                                            GZIPInputStream(fileInStream)
                                        }else {
                                            fileInStream
                                        }
                                        entryInStream.copyTo(zipOut)
                                        zipOut.closeEntry()
                                    }
                                }
                            }
                        }

                        val metadata = importersManager.extractMetadata(
                            uri = tmpZipFile.toDoorUri(),
                            originalFilename = filename
                        )

                        val jobItem = ContentEntryImportJob(
                            sourceUri = tmpZipFile.toDoorUri().toString(),
                            cjiOriginalFilename = filename,
                            cjiContentEntryUid = container.containerContentEntryUid,
                            cjiPluginId = metadata?.importerId ?: 0
                        )
                        val jobUid = contentEntryImportJobDao.insertJobItem(jobItem)
                        importUseCase(jobUid)

                        tmpZipFile.delete()
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