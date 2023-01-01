package com.ustadmobile.core.catalog.contenttype

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.guessMimeType
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class FolderIndexerPlugin(
        private var context: Any,
        private val endpoint: Endpoint,
        override val di: DI
): ContentPlugin {

    override val pluginId: Int
        get() = PLUGIN_ID
    override val supportedMimeTypes: List<String>
        get() = listOf()
    override val supportedFileExtensions: List<String>
        get() = listOf()

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val pluginManager = ContentPluginManager(listOf(
            di.on(endpoint).direct.instance<EpubTypePluginCommonJvm>(),
            di.on(endpoint).direct.instance<XapiTypePluginCommonJvm>(),
            di.on(endpoint).direct.instance<H5PTypePluginCommonJvm>(),
            di.on(endpoint).direct.instance<PDFTypePlugin>(),
            di.on(endpoint).direct.instance<VideoTypePluginAndroid>()))

    override suspend fun extractMetadata(uri: DoorUri, process: ContentJobProcessContext): MetadataResult? {

        val docUri = try {
            DocumentsContract.buildDocumentUriUsingTree(uri.uri, DocumentsContract.getDocumentId(uri.uri))
        }catch (e: Exception){
            DocumentsContract.buildDocumentUriUsingTree(uri.uri, DocumentsContract.getTreeDocumentId(uri.uri))
        }

        val doc = DocumentFile.fromSingleUri(context as Context, docUri) ?: return null

        if(!doc.isDirectory){
            return null
        }

        val entry = ContentEntryWithLanguage().apply {
            title = doc.name ?: DocumentsContract.getDocumentId(docUri)
            sourceUrl =  uri.uri.toString()
            leaf = false
            contentTypeFlag = ContentEntry.TYPE_COLLECTION
        }

        return MetadataResult(entry, PLUGIN_ID)
    }

    override suspend fun processJob(jobItem: ContentJobItemAndContentJob, process: ContentJobProcessContext, progress: ContentJobProgressListener): ProcessResult {
        val contentJobItem = jobItem.contentJobItem ?: throw IllegalArgumentException("missing job item")
        val jobUri = contentJobItem.sourceUri ?: return ProcessResult(JobStatus.FAILED)
        withContext(Dispatchers.Default) {
            val uri = DoorUri.parse(jobUri)

            val startingUri = try{
                DocumentsContract.buildChildDocumentsUriUsingTree(uri.uri, DocumentsContract.getDocumentId(uri.uri))
            }catch (e: Exception){
                DocumentsContract.buildChildDocumentsUriUsingTree(uri.uri, DocumentsContract.getTreeDocumentId(uri.uri))
            }

            val uriList = mutableListOf<Uri>()
            var cursor: Cursor? = null
            try {
                // let's query the files
                cursor = (context as Context).contentResolver.query(startingUri, arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID),
                        null, null, null)
                if (cursor?.moveToFirst() == true) {
                    do {
                        // build the uri for the file
                        val uriFile: Uri = DocumentsContract.buildDocumentUriUsingTree(startingUri, cursor.getString(0))
                        //add to the list
                        uriList.add(uriFile)
                    } while (cursor.moveToNext())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }

            contentJobItem.cjiItemTotal = uriList.size.toLong()
            progress.onProgress(contentJobItem)

            uriList.forEachIndexed { index, fileUri ->
                val fileDocument = DocumentFile.fromSingleUri(context as Context, fileUri) ?: return@forEachIndexed

                if(fileDocument.isDirectory){

                    ContentJobItem().apply {
                        cjiJobUid = contentJobItem.cjiJobUid
                        sourceUri = fileDocument.uri.toString()
                        cjiItemTotal = fileDocument.length()
                        cjiPluginId = PLUGIN_ID
                        cjiContentEntryUid = 0
                        cjiIsLeaf = false
                        cjiParentCjiUid = contentJobItem.cjiUid
                        cjiParentContentEntryUid = contentJobItem.cjiContentEntryUid
                        cjiConnectivityNeeded = false
                        cjiStatus = JobStatus.QUEUED
                        cjiUid = db.contentJobItemDao.insertJobItem(this)
                    }

                }else{

                    val hrefDoorUri = DoorUri.parse(fileDocument.uri.toString())
                    val mimeType = hrefDoorUri.guessMimeType(context, di)
                    val isSupported = mimeType?.let { pluginManager.isMimeTypeSupported(it) } ?: true

                    if(isSupported){

                        ContentJobItem().apply {
                            cjiJobUid = contentJobItem.cjiJobUid
                            sourceUri = fileDocument.uri.toString()
                            cjiItemTotal = fileDocument.length()
                            cjiContentEntryUid = 0
                            cjiIsLeaf = true
                            cjiPluginId = 0
                            cjiParentCjiUid = contentJobItem.cjiUid
                            cjiParentContentEntryUid = contentJobItem.cjiContentEntryUid
                            cjiConnectivityNeeded = false
                            cjiStatus = JobStatus.QUEUED
                            cjiUid = db.contentJobItemDao.insertJobItem(this)
                        }

                    }
                }

                contentJobItem.cjiItemProgress = index.toLong()
                progress.onProgress(contentJobItem)
            }

            contentJobItem.cjiItemProgress = uriList.size.toLong()
            progress.onProgress(contentJobItem)
        }

        return ProcessResult(JobStatus.COMPLETE)
    }

    companion object {

        const val PLUGIN_ID = 13

    }

}