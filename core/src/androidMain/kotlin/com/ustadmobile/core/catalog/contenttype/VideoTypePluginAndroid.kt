package com.ustadmobile.core.catalog.contenttype

import android.content.Context
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import io.github.aakira.napier.Napier
import com.linkedin.android.litr.MediaTransformer
import com.linkedin.android.litr.TransformationListener
import com.linkedin.android.litr.TransformationOptions
import com.linkedin.android.litr.analytics.TrackTransformationInfo
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.container.ContainerAddOptions
import com.ustadmobile.core.contentjob.*
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.*
import com.ustadmobile.core.network.NetworkProgressListenerAdapter
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.core.util.ext.*
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.lib.db.entities.*
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.File


class VideoTypePluginAndroid(
        private var context: Any,
        private val endpoint: Endpoint,
        override val di: DI,
        private val uploader: ContentPluginUploader = DefaultContentPluginUploader(di)
) : VideoTypePlugin() {

    private val VIDEO_ANDROID = "VideoPluginAndroid"

    private val repo: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_REPO)

    private val db: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val defaultContainerDir: File by di.on(endpoint).instance(tag = DiTag.TAG_DEFAULT_CONTAINER_DIR)

    private val httpClient: HttpClient = di.direct.instance()

    override suspend fun extractMetadata(uri: DoorUri, process: ContentJobProcessContext): MetadataResult? {
        return getEntry(uri, process)
    }

    override suspend fun processJob(jobItem: ContentJobItemAndContentJob, process: ContentJobProcessContext, jobProgress: ContentJobProgressListener): ProcessResult {
        val contentJobItem = jobItem.contentJobItem
                ?: throw IllegalArgumentException("missing job item")
        return withContext(Dispatchers.Default) {

            val uri = contentJobItem.sourceUri ?: throw IllegalStateException("missing uri")
            val videoUri = DoorUri.parse(uri)
            val contentNeedUpload = !videoUri.isRemote()
            val localUri = process.getLocalOrCachedUri()
            contentJobItem.updateTotalFromLocalUriIfNeeded(localUri, contentNeedUpload,
                jobProgress, context, di)

            val videoTempDir = makeTempDir(prefix = "tmp")
            val newVideo = File(videoTempDir,
                    localUri.getFileName(context))
            val params: Map<String, String> = safeParse(di, MapSerializer(String.serializer(), String.serializer()),
                jobItem.contentJob?.params ?: "")
            val compressVideo: Boolean = params["compress"]?.toBoolean() ?: false
            val mediaTransformer = MediaTransformer(context as Context)
            val videoIsProcessed = contentJobItem.cjiContainerUid != 0L

            try {

                if(!videoIsProcessed) {

                    Napier.d(tag = VIDEO_ANDROID, message = "conversion Params compress video is $compressVideo")

                    if (compressVideo) {

                        Napier.d(tag = VIDEO_ANDROID, message = "start import for new video file $newVideo.name")

                        val dimensionsArray = params["dimensions"]?.split("x") ?: listOf()
                        val storageDimensions = localUri.extractVideoResolutionMetadata(context as Context)
                        val originalVideoDimensions = if (dimensionsArray.isEmpty()) {
                            storageDimensions
                        } else {
                            val aspectRatio = dimensionsArray[0].toFloat() / dimensionsArray[1].toFloat()
                            Pair(storageDimensions.first, (storageDimensions.first / aspectRatio).toInt())
                        }
                        val newVideoDimensions = originalVideoDimensions.fitWithin()

                        Napier.d(tag = VIDEO_ANDROID, message = "width of old video is ${originalVideoDimensions.first}, height of old video is ${originalVideoDimensions.second}")
                        Napier.d(tag = VIDEO_ANDROID, message = "width of new video is ${newVideoDimensions.first}, height of new video is ${newVideoDimensions.second}")

                        val videoTarget = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, newVideoDimensions.first, newVideoDimensions.second).apply {
                            setInteger(MediaFormat.KEY_BIT_RATE, VIDEO_BIT_RATE)
                            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, VIDEO_FRAME_INTERVAL)
                            setInteger(MediaFormat.KEY_FRAME_RATE, VIDEO_FRAME_RATE)
                            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                        }

                        val audioCodecInfo = MediaExtractor().useThenRelease {
                            it.setDataSource(context as Context, localUri.uri, null)
                            it.getFirstAudioCodecInfo()
                        }

                        val audioTarget = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                                audioCodecInfo.sampleRate, audioCodecInfo.channelCount).apply {
                            setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BIT_RATE)
                        }

                        val videoCompleted = CompletableDeferred<Boolean>()

                        try {

                            mediaTransformer.transform(contentJobItem.cjiContentEntryUid.toString(), localUri.uri, newVideo.path,
                                    videoTarget, audioTarget, object : TransformationListener {
                                override fun onStarted(id: String) {
                                    Napier.d(tag = VIDEO_ANDROID, message = "started transform")
                                }

                                override fun onProgress(id: String, progress: Float) {
                                    Napier.d(tag = VIDEO_ANDROID, message = "progress at value ${progress * 100}")
                                    var cjiProgress = (progress * contentJobItem.cjiItemTotal)
                                    if(contentNeedUpload)
                                        cjiProgress /= 2.toFloat()

                                    contentJobItem.cjiItemProgress = cjiProgress.toLong()
                                    jobProgress.onProgress(contentJobItem)
                                }

                                override fun onCompleted(id: String, trackTransformationInfos: MutableList<TrackTransformationInfo>?) {
                                    Napier.d(tag = VIDEO_ANDROID, message = "completed transform")
                                    var cjiProgress = contentJobItem.cjiItemTotal
                                    if(contentNeedUpload)
                                        cjiProgress /= 2

                                    contentJobItem.cjiItemProgress = cjiProgress
                                    jobProgress.onProgress(contentJobItem)
                                    videoCompleted.complete(true)
                                }

                                override fun onCancelled(id: String, trackTransformationInfos: MutableList<TrackTransformationInfo>?) {
                                    Napier.d(tag = VIDEO_ANDROID, message = "cancelled transform")
                                    videoCompleted.complete(false)
                                }

                                override fun onError(id: String, cause: Throwable?, trackTransformationInfos: MutableList<TrackTransformationInfo>?) {
                                    videoCompleted.completeExceptionally(cause
                                            ?: RuntimeException("error on video id: $id"))
                                }

                            }, TransformationOptions.Builder()
                                    .setGranularity(MediaTransformer.GRANULARITY_DEFAULT)
                                    .setVideoFilters(null)
                                    .build()
                            )

                            videoCompleted.await()
                        } catch (e: Exception) {
                            if(e is CancellationException){
                                mediaTransformer.cancel(contentJobItem.cjiContentEntryUid.toString())
                                throw e
                            }
                            Napier.e(tag = VIDEO_ANDROID, throwable = e, message = e.message ?: "")
                            throw FatalContentJobException("ContentJobItem #${jobItem.contentJobItem?.cjiUid}: cannot compress video")
                        } finally {
                            mediaTransformer.release()
                        }

                    }


                    val container = db.containerDao.findByUid(contentJobItem.cjiContainerUid)
                            ?: Container().apply {
                                containerContentEntryUid = contentJobItem.cjiContentEntryUid
                                cntLastModified = System.currentTimeMillis()
                                mimeType = supportedMimeTypes.first()
                                containerUid = repo.containerDao.insertAsync(this)
                            }

                    contentJobItem.cjiContainerUid = container.containerUid
                    process.withContentJobItemTransactionMutex { txDb ->
                        txDb.contentJobItemDao.updateContentJobItemContainer(contentJobItem.cjiUid,
                            container.containerUid)
                    }


                    val containerFolder = jobItem.contentJob?.toUri
                            ?: defaultContainerDir.toURI().toString()
                    val containerFolderUri = DoorUri.parse(containerFolder)

                    if (compressVideo) {
                        repo.addFileToContainer(container.containerUid, newVideo.toDoorUri(), newVideo.name,
                                context,
                                di,
                                ContainerAddOptions(containerFolderUri))
                    } else {
                        repo.addContainerFromUri(container.containerUid, localUri, context, di,
                                localUri.getFileName(context),
                                ContainerAddOptions(containerFolderUri))
                    }

                    contentJobItem.updateTotalFromContainerSize(contentNeedUpload, db,
                        jobProgress)

                    val haveConnectivityToContinueJob = process.withContentJobItemTransactionMutex { txDb ->
                        txDb.contentJobItemDao.updateContainerProcessed(contentJobItem.cjiUid, true)

                        contentJobItem.cjiConnectivityNeeded = true
                        txDb.contentJobItemDao.updateConnectivityNeeded(contentJobItem.cjiUid, true)

                        txDb.contentJobDao.isConnectivityAcceptableForJob(jobItem.contentJob?.cjUid
                            ?: 0)
                    }

                    if (!haveConnectivityToContinueJob) {
                        return@withContext ProcessResult(JobStatus.WAITING_FOR_CONNECTION)
                    }

                }

                if(contentNeedUpload) {
                    return@withContext ProcessResult(uploader.upload(contentJobItem,
                        NetworkProgressListenerAdapter(jobProgress, contentJobItem),
                        httpClient, endpoint, process)
                    )
                }

                return@withContext ProcessResult(JobStatus.COMPLETE)
            }catch(c: CancellationException){

                withContext(NonCancellable){
                    newVideo.delete()
                    videoTempDir.deleteRecursively()
                    if(videoUri.isRemote()){
                        localUri.toFile().delete()
                    }
                }

                throw c
            }finally {
                videoTempDir.deleteRecursively()
                mediaTransformer.release()
            }
        }
    }

    suspend fun getEntry(doorUri: DoorUri, process: ContentJobProcessContext): MetadataResult? {
        return withContext(Dispatchers.Default) {

            val localUri = process.getLocalOrCachedUri()

            val fileName = localUri.getFileName(context)

            if (!supportedFileExtensions.any { fileName.endsWith(it, true) }) {
                return@withContext null
            }

            val fileVideoDimensions = localUri.extractVideoResolutionMetadata(context as Context)

            if (fileVideoDimensions.first == 0 || fileVideoDimensions.second == 0) {
                return@withContext null
            }

            val entry = ContentEntryWithLanguage().apply {
                this.title = fileName
                this.leaf = true
                this.sourceUrl = doorUri.uri.toString()
                this.contentTypeFlag = ContentEntry.TYPE_VIDEO
            }
            MetadataResult(entry, pluginId)
        }
    }

}