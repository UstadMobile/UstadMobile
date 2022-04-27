package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.ContentJobProcessContext
import com.ustadmobile.core.contentjob.ContentJobProgressListener
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.contentjob.ProcessResult
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.encodeBase64
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import com.ustadmobile.retriever.ext.copyToAsync
import com.ustadmobile.retriever.ext.fileChecksums
import com.ustadmobile.retriever.io.MultiDigestOutputStream
import com.ustadmobile.retriever.io.NullOutputStream
import io.github.aakira.napier.Napier
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * Adds to the integrity checksum for all existing ContainerEntryFile entities. Part of upgrading
 * to enabling retriever for downloads.
 */
class ContainerEntryFileIntegrityUpgradePlugin (
    context: Any,
    endpoint: Endpoint,
    di: DI
): AbstractContentEntryPlugin(context, endpoint, di){

    override val pluginId: Int
        get() = PLUGIN_ID
    override val supportedMimeTypes: List<String>
        get() = listOf()
    override val supportedFileExtensions: List<String>
        get() = listOf()

    override suspend fun extractMetadata(
        uri: DoorUri,
        process: ContentJobProcessContext
    ): MetadataResult? {
        return null
    }

    override suspend fun processJob(
        jobItem: ContentJobItemAndContentJob,
        process: ContentJobProcessContext,
        progress: ContentJobProgressListener
    ): ProcessResult {
        val db: UmAppDatabase = di.direct.on(endpoint).instance(tag = UmAppDatabase.TAG_DB)

        var entriesToChecksum: List<ContainerEntryFile>
        do {
            entriesToChecksum = db.containerEntryFileDao.findEntriesWithoutIntegrity(500)

            val messageDigest = MessageDigest.getInstance("SHA-256")
            entriesToChecksum.forEach { cef ->
                val filePath = cef.cefPath ?: return@forEach
                MultiDigestOutputStream(NullOutputStream(), arrayOf(messageDigest)).use { digestOut ->
                    FileInputStream(filePath).use { fileIn ->
                        fileIn.copyToAsync(digestOut)
                    }
                }
                val fileDigest = messageDigest.digest()
                cef.cefIntegrity = "sha256-" + fileDigest.encodeBase64()

                messageDigest.reset()
            }

            db.withDoorTransactionAsync(UmAppDatabase::class) { txDb ->
                entriesToChecksum.forEach {
                    txDb.containerEntryFileDao.updateIntegrity(it.cefUid, it.cefIntegrity!!)
                }
            }

            Napier.i("Generated checksum for ${entriesToChecksum.size}")
        }while(entriesToChecksum.isNotEmpty())


        return ProcessResult(JobStatus.COMPLETE)
    }

    companion object {

        const val PLUGIN_ID = 607



    }
}