package com.ustadmobile.core.catalog.contenttype

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import androidx.documentfile.provider.DocumentFile
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.getSize
import com.ustadmobile.core.util.createTemporaryDir
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.lib.db.entities.ContentJobItem
import com.ustadmobile.lib.db.entities.ContentJobItemAndContentJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File
import java.lang.Exception

class FolderIndexerPlugin(private var context: Any, private val endpoint: Endpoint, override val di: DI): ContentPlugin {

    override val pluginId: Int
        get() = PLUGIN_ID
    override val supportedMimeTypes: List<String>
        get() = listOf()
    override val supportedFileExtensions: List<String>
        get() = listOf()

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val pluginManager: ContentPluginManager by di.on(endpoint).instance()

    override suspend fun extractMetadata(uri: DoorUri, process: ProcessContext): MetadataResult? {

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

    override suspend fun processJob(jobItem: ContentJobItemAndContentJob, process: ProcessContext, progress: ContentJobProgressListener): ProcessResult {
        val contentJobItem = jobItem.contentJobItem ?: throw IllegalArgumentException("missing job item")
        val jobUri = contentJobItem.sourceUri ?: return ProcessResult(JobStatus.FAILED)
        withContext(Dispatchers.Default) {
            val uri = DoorUri.parse(jobUri)

            val apacheDir = createTemporaryDir("folder-${jobItem.contentJobItem?.cjiUid}")

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
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        // build the uri for the file
                        val uriFile: Uri = DocumentsContract.buildDocumentUriUsingTree(startingUri, cursor.getString(0))
                        //add to the list
                        uriList.add(uriFile)
                    } while (cursor.moveToNext())
                }
            } catch (e: Exception) {
                // TODO: handle error
            } finally {
                cursor?.close()
            }

            uriList.forEach { fileUri ->

                val fileDocument = DocumentFile.fromSingleUri(context as Context, fileUri)

                if(fileDocument?.isDirectory == true){

                    ContentJobItem().apply {
                        cjiJobUid = contentJobItem.cjiJobUid
                        sourceUri = fileDocument.uri.toString()
                        cjiItemTotal = fileDocument.length()
                        cjiPluginId = PLUGIN_ID
                        cjiContentEntryUid = 0
                        cjiIsLeaf = false
                        cjiParentContentEntryUid = contentJobItem.cjiContentEntryUid
                        cjiConnectivityAcceptable = ContentJobItem.ACCEPT_ANY
                        cjiStatus = JobStatus.QUEUED
                        cjiUid = db.contentJobItemDao.insertJobItem(this)
                    }

                }else{

                    val processContext = ProcessContext(apacheDir, mutableMapOf())
                    val hrefDoorUri = DoorUri.parse(fileDocument?.uri.toString())
                    val metadataResult = pluginManager.extractMetadata(hrefDoorUri, processContext)
                    if(metadataResult != null){

                        ContentJobItem().apply {
                            cjiJobUid = contentJobItem.cjiJobUid
                            sourceUri = fileDocument?.uri.toString()
                            cjiItemTotal = sourceUri?.let { source -> DoorUri.parse(source).getSize(context, di)  } ?: 0L
                            cjiContentEntryUid = 0
                            cjiIsLeaf = true
                            cjiPluginId = metadataResult.pluginId
                            cjiParentContentEntryUid = contentJobItem.cjiContentEntryUid
                            cjiConnectivityAcceptable = ContentJobItem.ACCEPT_ANY
                            cjiStatus = JobStatus.QUEUED
                            cjiUid = db.contentJobItemDao.insertJobItem(this)
                        }

                    }else{
                        println("no metadata found for file ${fileDocument?.name}")
                    }
                }
            }
        }
        return ProcessResult(JobStatus.COMPLETE)
    }

    companion object {

        const val PLUGIN_ID = 13

    }

}