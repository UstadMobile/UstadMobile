package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.getSize
import com.ustadmobile.core.io.ext.guessMimeType
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.openInputStream
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.InputStream
import java.net.URL

class ApacheIndexerPlugin(private var context: Any, private val endpoint: Endpoint, override val di: DI): ContentPlugin {


    override val pluginId: Int
        get() = PLUGIN_ID

    override val supportedMimeTypes: List<String>
        get() = listOf("text/html", "text/plain")

    override val supportedFileExtensions: List<String>
        get() = listOf()

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    //Plugins that supoprt direct download using HTTP
    private val pluginManager = ContentPluginManager(listOf(
            di.on(endpoint).direct.instance<EpubTypePluginCommonJvm>(),
            di.on(endpoint).direct.instance<XapiTypePluginCommonJvm>(),
            di.on(endpoint).direct.instance<H5PTypePluginCommonJvm>(),
            di.on(endpoint).direct.instance<PDFTypePluginJvm>(),
            di.on(endpoint).direct.instance<VideoTypePluginJvm>()))

    override suspend fun extractMetadata(uri: DoorUri, process: ContentJobProcessContext): MetadataResult? {
        val mimeType = uri.guessMimeType(context, di)?.substringBefore(";")
        if(mimeType != null && !supportedMimeTypes.contains(mimeType)){
            return null
        }
        return withContext(Dispatchers.Default) {


            val localUri = process.getLocalOrCachedUri()

            val data: InputStream? = localUri.openInputStream(context)
            val document = Jsoup.parse(data, "UTF-8", uri.uri.toURL().toString())

            val table = document.select("table tr th")
            if (table.isEmpty()) {
                return@withContext null
            }

            val altElement = table.select("[alt]")
            if (altElement.isNullOrEmpty() || altElement.attr("alt") != "[ICO]") {
                return@withContext null
            }

            val entry = ContentEntryWithLanguage()
            entry.title = document.title().substringAfterLast("/")
            entry.sourceUrl =  uri.uri.toString()
            entry.leaf = false
            entry.contentTypeFlag = ContentEntry.TYPE_COLLECTION

            return@withContext MetadataResult(entry, PLUGIN_ID)
        }
    }

    override suspend fun processJob(
        jobItem: ContentJobItemAndContentJob,
        process: ContentJobProcessContext,
        progress: ContentJobProgressListener
    ): ProcessResult {
        val contentJobItem = jobItem.contentJobItem ?: throw IllegalArgumentException("missing job item")
        val jobUri = contentJobItem.sourceUri ?: return ProcessResult(JobStatus.FAILED)
        withContext(Dispatchers.Default) {
            val uri = DoorUri.parse(jobUri)
            val localUri = process.getLocalOrCachedUri()
            val data: InputStream? = localUri.openInputStream(context)
            val document = Jsoup.parse(data, "UTF-8", uri.uri.toURL().toString())

            document.select("tr:has([alt])").forEachIndexed { counter, it ->

                val alt = it.select("td [alt]").attr("alt")
                try {
                    val element = it.select("td a")
                    val href = element.attr("href")
                    if(href.isEmpty()){
                        return@forEachIndexed
                    }
                    val hrefUrl = URL(uri.uri.toURL(), href)

                    if(alt == "[PARENTDIR]" || alt == "[ICO]" || alt == ""){
                        return@forEachIndexed
                    }

                    if (alt == "[DIR]") {

                        ContentJobItem().apply {
                            cjiJobUid = contentJobItem.cjiJobUid
                            sourceUri = hrefUrl.toURI().toString()
                            cjiItemTotal = 0
                            cjiPluginId = PLUGIN_ID
                            cjiContentEntryUid = 0
                            cjiIsLeaf = false
                            cjiParentContentEntryUid = contentJobItem.cjiContentEntryUid
                            cjiParentCjiUid = contentJobItem.cjiUid
                            cjiConnectivityNeeded = false
                            cjiStatus = JobStatus.QUEUED
                            cjiUid = db.contentJobItemDao.insertJobItem(this)
                        }

                    } else {

                        val hrefDoorUri = DoorUri.parse(hrefUrl.toURI().toString())
                        val mimeType = hrefDoorUri.guessMimeType(context, di)
                        val isSupported = mimeType?.let { pluginManager.isMimeTypeSupported(it) } ?: true

                        if(isSupported){

                            ContentJobItem().apply {
                                cjiJobUid = contentJobItem.cjiJobUid
                                sourceUri = hrefUrl.toURI().toString()
                                cjiItemTotal = sourceUri?.let { DoorUri.parse(it).getSize(context, di)  } ?: 0L
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
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return ProcessResult(JobStatus.COMPLETE)
    }


    companion object {


        const val PLUGIN_ID = 11
    }


}