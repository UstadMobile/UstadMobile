package com.ustadmobile.core.uri

import android.annotation.SuppressLint
import android.content.Context
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.getFileName
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.IOException

class UriHelperAndroid(private val appContext: Context): UriHelper{

    override suspend fun getMimeType(uri: DoorUri): String? {
        return appContext.contentResolver.getType(uri.uri)
    }

    override suspend fun getFileName(uri: DoorUri): String {
        return appContext.contentResolver.getFileName(uri.uri)
    }

    override suspend fun getSize(uri: DoorUri): Long {
        return -1
    }

    @SuppressLint("Recycle") //The input stream is closed when the source is closed.
    override suspend fun openSource(uri: DoorUri): Source {
        return appContext.contentResolver.openInputStream(uri.uri)?.asSource()?.buffered()
            ?: throw IOException("Could not open uri: $uri")
    }
}