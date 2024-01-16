package com.ustadmobile.core.contentformats.video

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.ContentImportProgressListener
import com.ustadmobile.core.contentformats.ContentImporter
import com.ustadmobile.core.contentformats.manifest.ContentManifest
import com.ustadmobile.core.contentformats.media.MediaContentInfo
import com.ustadmobile.core.contentformats.media.MediaSource
import com.ustadmobile.core.contentformats.storeText
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.contententry.ContentConstants
import com.ustadmobile.core.io.ext.toDoorUri
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.core.util.ext.requireSourceAsDoorUri
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import com.ustadmobile.libcache.UstadCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import kotlinx.serialization.json.Json
import java.io.File

abstract class AbstractVideoContentImporterCommonJvm(
    endpoint: Endpoint,
    protected val cache: UstadCache,
    protected val uriHelper: UriHelper,
    protected val json: Json,
    protected val fileSystem: FileSystem = SystemFileSystem,
    protected val db: UmAppDatabase,
    protected val tmpPath: Path,
    protected val saveLocalUriAsBlobAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase,
) : ContentImporter(endpoint) {

    /**
     * This is not responsible for checking that this is actually a video file, that is done by
     * extractmetadata.
     */
    override suspend fun importContent(
        jobItem: ContentEntryImportJob,
        progressListener: ContentImportProgressListener,
    ): ContentEntryVersion = withContext(Dispatchers.IO) {
        val jobUri = jobItem.requireSourceAsDoorUri()

        val contentEntryVersionUid = db.doorPrimaryKeyManager.nextId(ContentEntryVersion.TABLE_ID)
        val urlPrefix = createContentUrlPrefix(contentEntryVersionUid)
        val manifestUrl = "$urlPrefix${ContentConstants.MANIFEST_NAME}"
        val videoUrl = "${urlPrefix}video"
        val mediaInfoUrl = "${urlPrefix}media.json"
        val workDir = Path(tmpPath, "video-import-${systemTimeInMillis()}")
        fileSystem.createDirectories(workDir)

        val videoTmpFile = Path(workDir, "video")

        //Get the mime type from the uri to import if possible
        // If not, try looking at the original filename (might be needed where using temp import
        // files etc.
        val mimeType = uriHelper.getMimeType(jobUri)
            ?: jobItem.cjiOriginalFilename?.let {
                uriHelper.getMimeType(DoorUri.parse("file:///$it"))
            } ?: throw IllegalStateException("Cannot get mime type")

        val mediaContentInfo = MediaContentInfo(
            sources = listOf(
                MediaSource(
                    url = videoUrl,
                    mimeType = mimeType
                )
            )
        )

        val mediaInfoTmpFile = Path(workDir, "media.json")
        fileSystem.sink(mediaInfoTmpFile).buffered().use {
            it.writeString(
                json.encodeToString(
                    MediaContentInfo.serializer(), mediaContentInfo,
                )
            )
        }

        try {
            uriHelper.openSource(jobUri).use { uriSource ->
                fileSystem.sink(videoTmpFile).use { fileSink ->
                    uriSource.transferTo(fileSink)
                }
            }

            val savedManifestItems = saveLocalUriAsBlobAndManifestUseCase(
                listOf(
                    SaveLocalUriAsBlobAndManifestUseCase.SaveLocalUriAsBlobAndManifestItem(
                        blobItem = SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                            localUri = videoTmpFile.toDoorUri().toString(),
                            entityUid = contentEntryVersionUid,
                            tableId = ContentEntryVersion.TABLE_ID,
                            mimeType = mimeType,
                            deleteLocalUriAfterSave = true,
                        ),
                        manifestUri = "video"
                    ),
                    SaveLocalUriAsBlobAndManifestUseCase.SaveLocalUriAsBlobAndManifestItem(
                        blobItem = SaveLocalUrisAsBlobsUseCase.SaveLocalUriAsBlobItem(
                            localUri = mediaInfoTmpFile.toDoorUri().toString(),
                            entityUid = contentEntryVersionUid,
                            tableId = ContentEntryVersion.TABLE_ID,
                            mimeType = "application/json",
                            deleteLocalUriAfterSave = true,
                        ),
                        manifestUri = "media.json"
                    )
                )
            )

            val manifest = ContentManifest(
                version = 1,
                metadata = emptyMap(),
                entries = savedManifestItems.map { it.manifestEntry }
            )

            cache.storeText(
                url = manifestUrl,
                text = json.encodeToString(ContentManifest.serializer(), manifest),
                mimeType = "application/json"
            )


            ContentEntryVersion(
                cevUid = contentEntryVersionUid,
                cevContentType = ContentEntryVersion.TYPE_VIDEO,
                cevContentEntryUid = jobItem.cjiContentEntryUid,
                cevManifestUrl = manifestUrl,
                cevOpenUri = mediaInfoUrl,
            )
        }finally {
            File(workDir.toString()).deleteRecursively()
        }
    }
}