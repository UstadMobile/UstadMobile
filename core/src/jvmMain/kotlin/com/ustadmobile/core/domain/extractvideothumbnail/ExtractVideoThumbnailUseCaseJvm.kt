package com.ustadmobile.core.domain.extractvideothumbnail

import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.door.ext.toFile
import kotlinx.coroutines.CompletableDeferred
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import java.io.File

/**
 * Extract a thumbnail of a frame using VLC.
 *
 * See
 * https://github.com/caprica/vlcj-examples/blob/master/src/main/java/uk/co/caprica/vlcj/test/thumbs/ThumbsTest.java
 */
class ExtractVideoThumbnailUseCaseJvm : ExtractVideoThumbnailUseCase{

    override suspend fun invoke(
        videoUri: DoorUri,
        position: Float,
        destinationFilePath: String,
    ): ExtractVideoThumbnailUseCase.VideoThumbnailResult {
        val factory = MediaPlayerFactory(*VLC_ARGS)
        val mediaPlayer: MediaPlayer = factory.mediaPlayers().newMediaPlayer()

        try {
            val inPositionCompletable = CompletableDeferred<Unit>()
            val snapshotTakenCompletable = CompletableDeferred<Unit>()

            mediaPlayer.events().addMediaPlayerEventListener(object: MediaPlayerEventAdapter() {
                override fun positionChanged(mediaPlayer: MediaPlayer?, newPosition: Float) {
                    if(newPosition >= position * 0.9f) { /* 90% margin */
                        inPositionCompletable.complete(Unit)
                    }
                }

                override fun snapshotTaken(mediaPlayer: MediaPlayer?, filename: String?) {
                    snapshotTakenCompletable.complete(Unit)
                }
            })

            val mrl = if(videoUri.uri.scheme == "file") {
                videoUri.toFile().absolutePath
            }else {
                videoUri.toString()
            }

            if(mediaPlayer.media().start(mrl)) {
                val destFile = File(destinationFilePath)
                mediaPlayer.controls().setPosition(position)
                inPositionCompletable.await()
                mediaPlayer.snapshots().save(destFile)
                snapshotTakenCompletable.await()
                mediaPlayer.controls().stop()
                return ExtractVideoThumbnailUseCase.VideoThumbnailResult(
                    uri = destFile.toDoorUri(),
                    mimeType = "image/png",
                )
            }else {
                throw IllegalStateException("ExtractVideoThumbnailUseCaseJvm: Could not start playing: $mrl")
            }
        }finally {
            mediaPlayer.release()
        }

    }

    companion object {

        private val VLC_ARGS = arrayOf(
            "--intf", "dummy",  /* no interface */
            "--vout", "dummy",  /* we don't want video (output) */
            "--no-audio",  /* we don't want audio (decoding) */
            "--no-snapshot-preview"
        )
    }
}