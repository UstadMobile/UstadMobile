package com.ustadmobile.core.contentformats.h5p

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.contentformats.ContentImporter
import com.ustadmobile.core.contentformats.ContentImportProgressListener
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentformats.manifest.totalStorageSize
import com.ustadmobile.core.contentformats.storeText
import com.ustadmobile.core.contentjob.InvalidContentException
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressionLevel
import com.ustadmobile.core.domain.compress.list.CompressListUseCase
import com.ustadmobile.core.domain.compress.list.toItemToCompress
import com.ustadmobile.core.domain.compress.originalSizeHeaders
import com.ustadmobile.core.domain.contententry.ContentConstants
import com.ustadmobile.core.io.ext.readString
import com.ustadmobile.core.io.ext.skipToEntry
import com.ustadmobile.core.io.ext.toDoorUri
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.displayFilename
import com.ustadmobile.core.util.ext.fileExtensionOrNull
import com.ustadmobile.core.util.ext.requireSourceAsDoorUri
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.io.unzipTo
import io.github.aakira.napier.Napier
import io.ktor.util.escapeHTML
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.asInputStream
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * Import an H5P File. This is powered by H5P-Standalone ( https://github.com/tunapanda/h5p-standalone ).
 *
 * The H5P file will be converted into an xAPI file by bundling H5P-standalone and generating a
 * tincan.xml file.
 *
 * The entry will be stored as follows:
 *
 * prefix/h5p-folder - content of the H5P file itself
 * prefix/dist - content of the H5P standalone distribution zip (as per the GitHub release download)
 * from src/commonMain/resources/h5p/h5p-standalone-3.6.0.zip (unmodified).
 *
 * @param h5pInStream input stream provider for the h5p-standalone-3.6.0.zip. On JVM this is done via
 *        class resources. On Android this is done via assets.
 *
 */
class H5PContentImporter(
    learningSpace: LearningSpace,
    private val db: UmAppDatabase,
    private val cache: UstadCache,
    private val uriHelper: UriHelper,
    private val json: Json,
    private val tmpPath: Path,
    private val saveLocalUriAsBlobAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase,
    private val compressListUseCase: CompressListUseCase,
    private val fileSystem: FileSystem = SystemFileSystem,
    private val h5pInStream: () -> InputStream = {
        this::class.java.getResourceAsStream(
            "/h5p/h5p-standalone-3.6.0.zip"
        ) ?: throw IllegalStateException("Could not open h5p standalone zip")
    },
): ContentImporter(learningSpace) {

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
        val shouldBeH5p = originalFilename?.fileExtensionOrNull() == "h5p" ||
                uri.toString().displayFilename(removeExtension = false).fileExtensionOrNull()?.lowercase() == "h5p"
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
        val entry = db.contentEntryDao().findByUid(jobItem.cjiContentEntryUid)
        val params = CompressParams(
            compressionLevel = CompressionLevel.forValue(jobItem.cjiCompressionLevel)
        )

        val contentEntryVersionUid = db.doorPrimaryKeyManager.nextIdAsync(
            ContentEntryVersion.TABLE_ID)

        val workTmpPath = Path(tmpPath, "h5pimport-${systemTimeInMillis()}")
        fileSystem.createDirectories(workTmpPath)
        try {
            val h5pUnzippedPath = Path(workTmpPath, "h5p-folder")
            fileSystem.createDirectories(h5pUnzippedPath)

            val h5pZipEntries = uriHelper.openSource(jobUri).use { zipSource ->
                zipSource.unzipTo(h5pUnzippedPath)
            }

            val compressedEntries = compressListUseCase(
                items = h5pZipEntries.map { it.toItemToCompress() },
                params = params,
                workDir = workTmpPath,
                onProgress = {
                    progressListener.onProgress(
                        jobItem.copy(
                            cjiItemTotal = it.total,
                            cjiItemProgress = it.completed
                        )
                    )
                }
            )

            val h5pContentManifestEntries = saveLocalUriAsBlobAndManifestUseCase(
                items = compressedEntries.map { compressedEntry ->
                    SaveLocalUriAsBlobAndManifestUseCase.SaveLocalUriAsBlobAndManifestItem(
                        blobItem = SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                            localUri = compressedEntry.localUri,
                            entityUid = contentEntryVersionUid,
                            tableId = ContentEntryVersion.TABLE_ID,
                            mimeType = compressedEntry.mimeType,
                            extraHeaders = compressedEntry.compressedResult.originalSizeHeaders(),
                        ),
                        manifestUri = "h5p-folder/${compressedEntry.originalItem.name}",
                        manifestMimeType = compressedEntry.mimeType,
                    )
                }
            )

            val h5pStandAloneUnzippedPath = Path(workTmpPath, "h5p-standalone")


            //Store the H5P-standalone resources. Within the zip there is a folder called dist.
            val h5pStandAloneUnzippedEntries = h5pInStream().asSource().buffered().use { h5pIn ->
                h5pIn.unzipTo(h5pStandAloneUnzippedPath)
            }

            val h5pStandAloneManifestEntries = saveLocalUriAsBlobAndManifestUseCase(
                items = h5pStandAloneUnzippedEntries.map { unzippedEntry ->
                    SaveLocalUriAsBlobAndManifestUseCase.SaveLocalUriAsBlobAndManifestItem(
                        blobItem = SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                            localUri = unzippedEntry.path.toDoorUri().toString(),
                            entityUid = contentEntryVersionUid,
                            tableId = ContentEntryVersion.TABLE_ID,
                            deleteLocalUriAfterSave = true,
                        ),
                        manifestUri = unzippedEntry.name,
                    )
                }
            )

            val entryId = "${learningSpace.url}/ns/xapi/${jobItem.cjiContentEntryUid}"
            val tinCanXmlPath = Path(workTmpPath, "tincan.xml")
            fileSystem.sink(tinCanXmlPath).buffered().use {
                it.writeString(
                    """
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
            """.trimIndent())
                it.flush()
            }

            //As per https://github.com/tunapanda/h5p-standalone
            val indexHtmlPath = Path(workTmpPath, "index.html")

            val h5pJsonUnzippedEntry = h5pZipEntries.first { it.name == "h5p.json" }
            val h5pJsonObj = json.parseToJsonElement(
                fileSystem.source(h5pJsonUnzippedEntry.path).buffered().readString()
            ).jsonObject
            val h5pTitle = h5pJsonObj["title"]?.jsonPrimitive?.contentOrNull
                ?: jobItem.cjiOriginalFilename?.displayFilename() ?: ""

            //As per https://github.com/tunapanda/h5p-standalone
            fileSystem.sink(indexHtmlPath).buffered().use {
                it.writeString(
                    """
                <html>
                <head>
                <meta charset="utf-8"/> 
                <title>${h5pTitle.escapeHTML()}</title>
                <script src="dist/main.bundle.js" type="text/javascript">
                </script>
                </head>
                
                <body>
                <div id='h5p-container'>
                </div>
                
                <script type='text/javascript'>
                
                let searchParams = new URLSearchParams(document.location.search);
                const el = document.getElementById('h5p-container');
                const baseUserDataUrl = searchParams.get("endpoint") + 
                            "activities/h5p-userdata?" + 
                            "Authorization=" + encodeURIComponent(searchParams.get("auth")) +
                            "&activityId=" + encodeURIComponent(searchParams.get("activity_id")) + 
                            "&agent=" + encodeURIComponent(searchParams.get("actor")) + 
                            "&registration=" + searchParams.get("registration");
                
                fetch(baseUserDataUrl + "&preload=1").then((preloadResponse) => {
                    preloadResponse.json().then((preloadedUserData) => {
                        console.log("Preloaded data: " + JSON.stringify(preloadedUserData));
                        const options = {
                            h5pJsonPath: './h5p-folder',
                            frameJs: 'dist/frame.bundle.js',
                            frameCss: 'dist/styles/h5p.css',
                            xAPIObjectIRI: searchParams.get("activity_id"),
                            contentUserData: preloadedUserData,
                            saveFreq: 2,
                            ajax: {
                                contentUserDataUrl: baseUserDataUrl + 
                                   "&stateId=:dataType" +
                                   "&subContentId=:subContentId" 
                            },
                            user: {
                                name : "a user",
                                email: "user@example.org",
                            }
                        };
                        
                        new H5PStandalone.H5P(el, options).then(function () {
                            H5P.externalDispatcher.on("xAPI", (event) => {
                              //do something useful with the event
                              const newStatement = (typeof(structuredClone) != "undefined") ? 
                                structuredClone(event.data.statement) : JSON.parse(JSON.stringify(event.data.statement));
                              newStatement.actor = JSON.parse(searchParams.get("actor"));
                              
                              let context = newStatement.context || {};
                              context.registration = searchParams.get("registration");
                              newStatement.context = context;
                              
                              console.log("xAPI statement: ", newStatement);
                              
                              fetch(searchParams.get("endpoint") + "statements?statementId=" + self.crypto.randomUUID(), {
                                method: "PUT",
                                headers: {
                                  "Content-Type": "application/json",
                                  "Authorization": searchParams.get("auth"),
                                },
                                body: JSON.stringify(newStatement),
                              });
                            });
                        });
                    });
                });
                        
                </script>
                
                </body>
                </html>
                """.trimIndent()
                )
            }

            val tinCanAndIndexEntries = saveLocalUriAsBlobAndManifestUseCase(
                items = listOf(
                    SaveLocalUriAsBlobAndManifestUseCase.SaveLocalUriAsBlobAndManifestItem(
                        blobItem = SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                            localUri = tinCanXmlPath.toDoorUri().toString(),
                            entityUid = contentEntryVersionUid,
                            tableId = ContentEntryVersion.TABLE_ID,
                            mimeType = "application/xml",
                            deleteLocalUriAfterSave = true
                        ),
                        manifestUri = "tincan.xml"
                    ),
                    SaveLocalUriAsBlobAndManifestUseCase.SaveLocalUriAsBlobAndManifestItem(
                        blobItem = SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                            localUri = indexHtmlPath.toDoorUri().toString(),
                            entityUid = contentEntryVersionUid,
                            tableId = ContentEntryVersion.TABLE_ID,
                            mimeType = "text/html",
                            deleteLocalUriAfterSave = true
                        ),
                        manifestUri = "index.html"
                    )
                )
            )

            val manifest = ContentManifest(
                version = 1,
                metadata = emptyMap(),
                entries = buildList {
                    addAll(h5pContentManifestEntries.map { it.manifestEntry })
                    addAll(h5pStandAloneManifestEntries.map { it.manifestEntry} )
                    addAll(tinCanAndIndexEntries.map { it.manifestEntry })
                }
            )

            val manifestUrl = "${createContentUrlPrefix(contentEntryVersionUid)}${ContentConstants.MANIFEST_NAME}"
            cache.storeText(
                url = manifestUrl,
                text = json.encodeToString(ContentManifest.serializer(), manifest),
                mimeType = "application/json"
            )

            ContentEntryVersion(
                cevUid = contentEntryVersionUid,
                cevContentType = ContentEntryVersion.TYPE_XAPI,
                cevManifestUrl = manifestUrl,
                cevContentEntryUid = jobItem.cjiContentEntryUid,
                cevOpenUri = "tincan.xml",
                cevStorageSize = h5pContentManifestEntries.totalStorageSize() +
                    h5pStandAloneManifestEntries.totalStorageSize() +
                    tinCanAndIndexEntries.totalStorageSize(),
                cevOriginalSize = uriHelper.getSize(jobUri),
            )
        }finally {
            File(workTmpPath.toString()).deleteRecursively()
        }
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