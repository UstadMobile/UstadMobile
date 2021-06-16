package com.ustadmobile.core.io.ext

import android.content.Context
import android.media.MediaMetadataRetriever
import android.webkit.MimeTypeMap
import com.ustadmobile.door.DoorUri

actual suspend fun DoorUri.guessMimeType(): String? {
    return MimeTypeMap.getFileExtensionFromUrl(this.toString())?.let { extension ->
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
}

actual suspend fun DoorUri.getSize(context: Any): Long {
    return (context as Context).contentResolver.openAssetFileDescriptor(uri, "r")?.length ?: 0
}

fun DoorUri.extractVideoResolutionMetadata(context: Context): Pair<Int, Int>{
    val metaRetriever = MediaMetadataRetriever()
    metaRetriever.setDataSource(context, this.uri)
    val originalHeight = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
    val originalWidth = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
    metaRetriever.release()

    return Pair(originalWidth, originalHeight)
}
