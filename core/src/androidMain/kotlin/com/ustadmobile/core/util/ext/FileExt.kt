package com.ustadmobile.core.util.ext

import android.media.MediaMetadataRetriever
import java.io.File


fun File.extractVideoResolutionMetadata(): Pair<Int, Int>{
    val metaRetriever = MediaMetadataRetriever()
    metaRetriever.setDataSource(this.path)
    val originalHeight = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
    val originalWidth = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
    metaRetriever.release()

    return Pair(originalWidth, originalHeight)
}

