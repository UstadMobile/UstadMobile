package com.ustadmobile.core.contentformats.h5p

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.ContentImporter
import com.ustadmobile.core.contentformats.ContentImportProgressListener
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.readString
import com.ustadmobile.core.io.ext.skipToEntry
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.requireSourceAsDoorUri
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.libcache.CacheEntryToStore
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.request.requestBuilder
import com.ustadmobile.libcache.response.StringResponse
import io.github.aakira.napier.Napier
import io.ktor.util.escapeHTML
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.asInputStream
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.zip.ZipInputStream

/**
 * Import an H5P File. This is powered by H5P-Standalone ( https://github.com/tunapanda/h5p-standalone ).
 *
 * The H5P file will be converted into an xAPI file by bundling H5P-standalone and generating a
 * tincan.xml file.
 *
 * The entry will be cached as follows:
 *
 * prefix/h5p-folder - content of the H5P file itself
 * prefix/dist - content of the H5P standalone distribution zip (as per the GitHub release download)
 * from src/commonMain/resources/h5p/h5p-standalone-3.6.0.zip (unmodified).
 *
 */
class H5PContentImportPlugin(
    endpoint: Endpoint,
    private val db: UmAppDatabase,
    private val cache: UstadCache,
    private val uriHelper: UriHelper,
    private val json: Json,
): ContentImporter(endpoint) {

    override val importerId: Int
        get() = PLUGIN_ID
    override val supportedMimeTypes: List<String>
        get() = listOf("application/zip")
    override val supportedFileExtensions: List<String>
        get() = listOf("h5p")

    override val formatName: String
        get() = "H5P"

    override suspend fun extractMetadata(
        uri: DoorUri,
        originalFilename: String?
    ): MetadataResult? = withContext(Dispatchers.IO) {
        val shouldBeH5p = originalFilename?.substringAfterLast(".")?.lowercase()
            ?.endsWith("h5p") == true

        try {
            val h5pJsonText = ZipInputStream(uriHelper.openSource(uri).asInputStream()).use { zipIn ->
                zipIn.skipToEntry { it.name == "h5p.json" }
                    ?: throw IllegalArgumentException("No h5p.json found")

                zipIn.readString()
            }

            val h5pJsonObj = json.parseToJsonElement(h5pJsonText).jsonObject

            /**
             * H5P.json file has a property authors, which is used as follows:
             *
             * authors : [
             *    {"name": <name>, "role": "Author|Editor|Licensee|Originator" },
             * ]
             * We filter by role author and then join the names of authors together
             */
            val metadataAuthor = h5pJsonObj["authors"]?.jsonArray?.filter {
                it.jsonObject["role"]?.jsonPrimitive?.contentOrNull == "Author"
            }?.mapNotNull {
                it.jsonObject["name"]?.jsonPrimitive?.contentOrNull
            }?.joinToString() ?: ""
            val metadataTitle = h5pJsonObj["title"]?.jsonPrimitive?.contentOrNull

            return@withContext MetadataResult(
                entry = ContentEntryWithLanguage().apply {
                    contentTypeFlag = ContentEntry.TYPE_INTERACTIVE_EXERCISE
                    title = if(!metadataTitle.isNullOrBlank()) metadataTitle else originalFilename
                    author = metadataAuthor
                    leaf = true
                    sourceUrl = uri.toString()
                    licenseType = LICENSE_MAP[h5pJsonObj["license"]?.jsonPrimitive?.contentOrNull ?: ""]
                        ?: ContentEntry.LICENSE_TYPE_OTHER
                    description = h5pJsonObj["authorComments"]?.jsonPrimitive?.contentOrNull ?: ""
                },
                importerId = importerId,
                originalFilename = originalFilename,
            )
        }catch(e: Throwable) {
            if(shouldBeH5p) {
                throw InvalidContentException("Invalid h5p file: ${e.message}", e)
            }else {
                Napier.d("H5pContentImport: cannot import $uri : ${e.message}")
                return@withContext null
            }
        }
    }


    override suspend fun importContent(
        jobItem: ContentEntryImportJob,
        progressListener: ContentImportProgressListener
    ): ContentEntryVersion = withContext(Dispatchers.IO) {
        val jobUri = jobItem.requireSourceAsDoorUri()
        val entry = db.contentEntryDao.findByUid(jobItem.cjiContentEntryUid)

        val contentEntryVersionUid = db.doorPrimaryKeyManager.nextIdAsync(
            ContentEntryVersion.TABLE_ID)
        val urlPrefix = createContentUrlPrefix(contentEntryVersionUid)

        //Store the h5p itself
        uriHelper.openSource(jobUri).use { zipSource ->
            cache.storeZip(
                zipSource = zipSource,
                urlPrefix = "${urlPrefix}h5p-folder/",
                retain = true
            )
        }

        //Store the H5P-standalone resources. Within the zip there is a folder called dist.
        this::class.java.getResourceAsStream(
            "/h5p/h5p-standalone-3.6.0.zip"
        )?.asSource()?.buffered()?.use { h5pIn ->
            cache.storeZip(
                zipSource = h5pIn,
                urlPrefix = urlPrefix,
                retain = true
            )
        } ?: throw IllegalStateException("Could not open h5p resource")

        val entryId = "${endpoint.url}/ns/xapi/${jobItem.cjiContentEntryUid}"

        val tinCanXml = """
            <?xml version="1.0" encoding="UTF-8"?>
                <tincan xmlns="http://projecttincan.com/tincan.xsd">
                    <activities>
                        <activity id="${entryId.escapeHTML()}" type="http://adlnet.gov/expapi/activities/module">
                            <name>${entry?.title?.escapeHTML()}</name>
                            <description lang="en-US">${entry?.description?.escapeHTML()}</description>
                            <launch lang="en-us">index.html</launch>
                        </activity>
                    </activities>
                </tincan>
        """.trimIndent()

        //As per https://github.com/tunapanda/h5p-standalone
        val indexHtml = """
            <html>
            <head>
            <script src="dist/main.bundle.js" type="text/javascript">
            </script>
            </head>

            <body>
            <div id='h5p-container'>
            </div>

            <script type='text/javascript'>

            const el = document.getElementById('h5p-container');
            const options = {
                h5pJsonPath: './h5p-folder',
                frameJs: 'dist/frame.bundle.js',
                frameCss: 'dist/styles/h5p.css',
            };
            new H5PStandalone.H5P(el, options);
            		
            </script>

            </body>
            </html>

        """.trimIndent()

        cache.store(
            listOf(
                requestBuilder("${urlPrefix}tincan.xml").let {
                    CacheEntryToStore(
                        request = it,
                        response = StringResponse(
                            request = it,
                            mimeType = "application/xml",
                            body = tinCanXml
                        )
                    )
                },
                requestBuilder("${urlPrefix}index.html").let {
                    CacheEntryToStore(
                        request = it,
                        response = StringResponse(
                            request = it,
                            mimeType = "text/html",
                            body = indexHtml,
                        )
                    )
                }
            )
        )

        ContentEntryVersion(
            cevUid = contentEntryVersionUid,
            cevContentType = ContentEntryVersion.TYPE_XAPI,
            cevContentEntryUid = jobItem.cjiContentEntryUid,
            cevUrl = "${urlPrefix}tincan.xml"
        )
    }

    companion object {

        const val PLUGIN_ID = 424

        val LICENSE_MAP = mapOf(
            "CC BY" to ContentEntry.LICENSE_TYPE_CC_BY,
            "CC BY-SA" to ContentEntry.LICENSE_TYPE_CC_BY_SA,
            "CC BY-ND" to ContentEntry.LICENSE_TYPE_CC_BY,
            "CC BY-NC" to ContentEntry.LICENSE_TYPE_OTHER,
            "CC BY-NC-SA" to ContentEntry.LICENSE_TYPE_CC_BY_NC_SA,
            "CC CC-BY-NC-CD" to ContentEntry.LICENSE_TYPE_OTHER,
            "CC0 1.0" to ContentEntry.LICENSE_TYPE_CC_0,
            "GNU GPL" to ContentEntry.LICENSE_TYPE_OTHER,
            "PD" to ContentEntry.LICENSE_TYPE_PUBLIC_DOMAIN,
            "ODC PDDL" to ContentEntry.LICENSE_TYPE_OTHER,
            "CC PDM" to ContentEntry.LICENSE_TYPE_OTHER,
            "C" to ContentEntry.ALL_RIGHTS_RESERVED,
            "U" to ContentEntry.LICENSE_TYPE_OTHER
        )

    }

}