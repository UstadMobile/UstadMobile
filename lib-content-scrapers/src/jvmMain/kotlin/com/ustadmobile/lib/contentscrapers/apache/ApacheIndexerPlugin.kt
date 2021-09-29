package com.ustadmobile.lib.contentscrapers.apache

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.contentjob.ext.processMetadata
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.getLocalUri
import com.ustadmobile.core.io.ext.getSize
import com.ustadmobile.core.io.ext.guessMimeType
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import com.ustadmobile.lib.db.entities.*
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.ustadmobile.door.ext.openInputStream
import org.apache.commons.lang3.exception.ExceptionUtils
import org.jsoup.Jsoup
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import java.io.InputStream
import java.net.URL

class ApacheIndexerPlugin(private var context: Any, private val endpoint: Endpoint, override val di: DI): ContentPlugin {

    private val logPrefix = "[ApacheIndexer] "

    override val pluginId: Int
        get() = PLUGIN_ID

    override val supportedMimeTypes: List<String>
        get() = listOf("text/html")

    override val supportedFileExtensions: List<String>
        get() = listOf()

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)


    override suspend fun extractMetadata(uri: DoorUri, process: ProcessContext): MetadataResult? {
        val mimeType = uri.guessMimeType(context, di)?.substringBefore(";")
        if(mimeType != null && !supportedMimeTypes.contains(mimeType)){
            return null
        }
        return withContext(Dispatchers.Default) {


            val localUri = process.getLocalUri(uri, context, di)

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

            val urlWithEndingSlash = uri.uri.toString().requirePostfix("/")
            val entry = ContentEntryWithLanguage()
            entry.title = document.title().substringAfterLast("/")
            entry.sourceUrl = urlWithEndingSlash
            entry.leaf = false
            entry.contentTypeFlag = ContentEntry.TYPE_COLLECTION

            return@withContext MetadataResult(entry, PLUGIN_ID)
        }
    }

    override suspend fun processJob(jobItem: ContentJobItemAndContentJob, process: ProcessContext, progress: ContentJobProgressListener): ProcessResult {
        val contentJobItem = jobItem.contentJobItem ?: throw IllegalArgumentException("missing job item")
        val jobUri = contentJobItem.sourceUri ?: return ProcessResult(JobStatus.FAILED)
        withContext(Dispatchers.Default) {
            val uri = DoorUri.parse(jobUri)
            val localUri = process.getLocalUri(uri, context, di)
            val contentEntryUid = processMetadata(jobItem, process, context,endpoint)

            val data: InputStream? = localUri.openInputStream(context)
            val document = Jsoup.parse(data, "UTF-8", uri.uri.toURL().toString())

            document.select("tr:has([alt])").forEachIndexed { counter, it ->

                val alt = it.select("td [alt]").attr("alt")
                try {
                    val element = it.select("td a")
                    val href = element.attr("href")
                    val title = element.text()
                    val hrefUrl = URL(uri.uri.toURL(), href)

                    if(alt == "[PARENTDIR]" || alt == "[ICO]"){
                        return@forEachIndexed
                    }

                    if (alt == "[DIR]") {

                        ContentJobItem().apply {
                            cjiJobUid = contentJobItem.cjiJobUid
                            sourceUri = hrefUrl.toURI().toString()
                            cjiItemTotal = sourceUri?.let { DoorUri.parse(it).getSize(context, di)  } ?: 0L
                            cjiPluginId = PLUGIN_ID
                            cjiContentEntryUid = 0
                            cjiIsLeaf = false
                            cjiParentContentEntryUid = contentEntryUid
                            cjiConnectivityAcceptable = ContentJobItem.ACCEPT_ANY
                            cjiStatus = JobStatus.QUEUED
                            cjiUid = db.contentJobItemDao.insertJobItem(this)
                        }

                        Napier.i("$logPrefix found new directory with title $title for parent folder ${document.title()}", tag = ScraperConstants.SCRAPER_TAG)

                    } else {


                        ContentJobItem().apply {
                            cjiJobUid = contentJobItem.cjiJobUid
                            sourceUri = hrefUrl.toURI().toString()
                            cjiItemTotal = sourceUri?.let { DoorUri.parse(it).getSize(context, di)  } ?: 0L
                            cjiContentEntryUid = 0
                            cjiIsLeaf = false
                            cjiParentContentEntryUid = contentEntryUid
                            cjiConnectivityAcceptable = ContentJobItem.ACCEPT_ANY
                            cjiStatus = JobStatus.QUEUED
                            cjiUid = db.contentJobItemDao.insertJobItem(this)
                        }

                    }
                } catch (e: Exception) {
                    Napier.e("$logPrefix Error during directory search on $jobUri", tag = ScraperConstants.SCRAPER_TAG)
                    Napier.e("$logPrefix ${ExceptionUtils.getStackTrace(e)}", tag = ScraperConstants.SCRAPER_TAG)
                }
            }
        }
        return ProcessResult(JobStatus.COMPLETE)
    }


    companion object {


        const val PLUGIN_ID = 11
    }


}