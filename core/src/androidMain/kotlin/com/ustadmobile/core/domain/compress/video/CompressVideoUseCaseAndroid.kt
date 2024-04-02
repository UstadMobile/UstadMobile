package com.ustadmobile.core.domain.compress.video

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import com.otaliastudios.transcoder.Transcoder
import com.otaliastudios.transcoder.TranscoderListener
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy
import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressProgressUpdate
import com.ustadmobile.core.domain.compress.CompressResult
import com.ustadmobile.core.domain.compress.CompressUseCase
import com.ustadmobile.core.ext.requireExtension
import com.ustadmobile.core.uri.UriHelper
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import java.io.File
import java.util.UUID

class CompressVideoUseCaseAndroid(
    private val appContext: Context,
    private val uriHelper: UriHelper,
): CompressUseCase {

    override suspend fun invoke(
        fromUri: String,
        toUri: String?,
        params: CompressParams,
        onProgress: CompressUseCase.OnCompressProgress?
    ): CompressResult {
        //As per https://developer.android.com/media/platform/supported-formats
        val vidStrategy = DefaultVideoStrategy
            .atMost(720, 1280)
            .bitRate(500 * 1000)
            .frameRate(30)
            .mimeType("video/avc")
            .build()

        val destFile = if(toUri != null) {
            Uri.parse(toUri).toFile().requireExtension("mp4")
        }else {
            File(appContext.cacheDir, UUID.randomUUID().toString() + ".mp4")
        }

        val sizeIn = uriHelper.getSize(DoorUri.parse(fromUri))
        Napier.v { "CompressVideoUseCase: video size in: $sizeIn " }

        val completable = CompletableDeferred<Int>()
        val transcoder = Transcoder.into(destFile.absolutePath)
            .addDataSource(appContext, Uri.parse(fromUri))
            .setVideoTrackStrategy(vidStrategy)
            .setListener(object: TranscoderListener {
                override fun onTranscodeProgress(progress: Double) {
                    Napier.v { "CompressVideoUseCase: progress: $progress completed=(${(progress * sizeIn).toLong()}" }
                    onProgress?.invoke(
                        CompressProgressUpdate(
                            fromUri = fromUri,
                            completed = (progress * sizeIn).toLong(),
                            total = sizeIn
                        )
                    )
                }

                override fun onTranscodeCompleted(p0: Int) {
                    Napier.v { "CompressVideoUseCase: completed: $p0" }
                    completable.complete(p0)
                }

                override fun onTranscodeCanceled() {

                }

                override fun onTranscodeFailed(p0: Throwable) {
                    Napier.e(throwable = p0) { "CompressVideoCase: failed"}
                    completable.completeExceptionally(p0)

                }
            })

        try {
            val future = transcoder.transcode()
            try {
                completable.await()
            }catch(e: CancellationException) {
                future.cancel(true)
                throw e
            }
        }catch(e2: Throwable) {
            Napier.e("CompressVideoUseCase: Exception", e2)
        }


        return CompressResult(
            uri = destFile.toDoorUri().toString(),
            mimeType = "video/avc"
        )
    }
}