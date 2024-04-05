package com.ustadmobile.core.uri

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.net.toFile
import com.ustadmobile.core.util.ext.getFileNameAndSize
import com.ustadmobile.door.DoorUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.IOException

class UriHelperAndroid(private val appContext: Context): UriHelper{

    override suspend fun getMimeType(uri: DoorUri): String? {
        return appContext.contentResolver.getType(uri.uri)
    }

    override suspend fun getFileName(uri: DoorUri): String {
        return appContext.contentResolver.getFileNameAndSize(uri.uri).first
    }

    override suspend fun getSize(uri: DoorUri): Long {
        return if(uri.uri.scheme == "file") {
            withContext(Dispatchers.IO) { uri.uri.toFile().length() }
        }else {
            appContext.contentResolver.getFileNameAndSize(uri.uri).second
        }
    }

    @SuppressLint("Recycle") //The input stream is closed when the source is closed.
    override suspend fun openSource(uri: DoorUri): Source {
        return appContext.contentResolver.openInputStream(uri.uri)?.asSource()?.buffered()
            ?: throw IOException("Could not open uri: $uri")
    }
}