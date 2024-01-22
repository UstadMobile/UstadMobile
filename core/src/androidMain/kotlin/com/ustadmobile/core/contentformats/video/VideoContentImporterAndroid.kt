package com.ustadmobile.core.contentformats.video

import android.content.Context
import android.media.MediaMetadataRetriever
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.media.VideoConstants
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.blob.saveandmanifest.SaveLocalUriAsBlobAndManifestUseCase
import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import com.ustadmobile.libcache.UstadCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json

/**
 * Could consider using videocompressor based on telegram:
 * https://github.com/AbedElazizShe/LightCompressor
 */
class VideoContentImporterAndroid(
    endpoint: Endpoint,
    cache: UstadCache,
    private val appContext: Context,
    uriHelper: UriHelper,
    tmpPath: Path,
    db: UmAppDatabase,
    fileSystem: FileSystem = SystemFileSystem,
    saveLocalUriAsBlobAndManifestUseCase: SaveLocalUriAsBlobAndManifestUseCase,
    json: Json,
    getStoragePathForUrlUseCase: GetStoragePathForUrlUseCase,
) : AbstractVideoContentImporterCommonJvm(
    endpoint = endpoint,
    uriHelper = uriHelper,
    cache = cache,
    db = db,
    tmpPath = tmpPath,
    fileSystem = fileSystem,
    saveLocalUriAsBlobAndManifestUseCase = saveLocalUriAsBlobAndManifestUseCase,
    json = json,
    getStoragePathForUrlUseCase = getStoragePathForUrlUseCase,
) {
    override val importerId: Int
        get() = 101
    override val supportedMimeTypes: List<String>
        get() = listOf("video/mpeg")

    override val supportedFileExtensions: List<String>
        get() = listOf("mp4", "m4v", "webm", "qt", "ogv", "avi", "mkv")
    override val formatName: String
        get() = "Video(MP4, M4V, Quicktime, WEBM, OGV, AVI)"
    override suspend fun extractMetadata(
        uri: DoorUri,
        originalFilename: String?
    ): MetadataResult? = withContext(Dispatchers.IO) {
        val hasVideoExtension = originalFilename?.substringAfterLast(".")?.lowercase()?.let {
            it in VideoConstants.VIDEO_EXT_LIST
        } ?: false

        //Check if this looks like it should be a video file
        if(!hasVideoExtension && uriHelper.getMimeType(uri)?.startsWith("video/") != true) {
            return@withContext null
        }

        val metaRetriever = MediaMetadataRetriever()
        try {
            metaRetriever.setDataSource(appContext, uri.uri)
            val videoHeight = metaRetriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            if(videoHeight > 0) {
                MetadataResult(
                    entry = ContentEntryWithLanguage().apply {
                        title = originalFilename ?: uri.toString().substringAfterLast("/")
                            .substringBefore("?")
                        leaf = true
                        sourceUrl = uri.toString()
                        contentTypeFlag = ContentEntry.TYPE_VIDEO
                    },
                    importerId = importerId,
                    originalFilename = originalFilename,
                )
            }else {
                null
            }
        }finally {
            metaRetriever.release()
        }
    }

}