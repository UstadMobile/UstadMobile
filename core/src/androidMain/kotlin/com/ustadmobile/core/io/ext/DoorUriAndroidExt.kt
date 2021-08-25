package com.ustadmobile.core.io.ext

import android.R.attr
import android.content.ContentResolver
import android.content.Context
import android.media.MediaMetadataRetriever
import android.webkit.MimeTypeMap
import com.ustadmobile.door.DoorUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.closeQuietly
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import java.io.IOException




actual suspend fun DoorUri.guessMimeType(context: Any, di: DI): String? {
    if(isRemote()){
        return withContext(Dispatchers.IO){
            var response: Response? = null
            try {
                val okHttpClient: OkHttpClient = di.direct.instance()
                val okRequest = Request.Builder().url(uri.toString()).head().build()
                response = okHttpClient.newCall(okRequest).execute()
                response.header("Content-Type")
            }catch (io: IOException){
                throw io
            }catch (e: Exception) {
                return@withContext null
            }finally {
                response?.closeQuietly()
            }
        }
    }else if(ContentResolver.SCHEME_CONTENT == uri.scheme){
        val cr: ContentResolver = (context as Context ).contentResolver
        return cr.getType(uri)
    }else{
       return MimeTypeMap.getSingleton().getMimeTypeFromExtension(
               MimeTypeMap.getFileExtensionFromUrl(uri.toString()).lowercase())
    }
}

actual suspend fun DoorUri.getSize(context: Any, di: DI): Long {
    return if(isRemote()){
        withContext(Dispatchers.IO){
            var response: Response? = null
            try {
                val okHttpClient: OkHttpClient = di.direct.instance()
                val okRequest = Request.Builder().url(uri.toString()).head().build()
                response = okHttpClient.newCall(okRequest).execute()
                val length = response.header("Content-Length")
                length?.toLong() ?: -1
            }catch (io: IOException){
                throw io
            }catch (e: Exception) {
                return@withContext -1
            }finally {
                response?.closeQuietly()
            }
        }
    } else{
        (context as Context).contentResolver.openAssetFileDescriptor(uri, "r")?.length ?: -1
    }
}

fun DoorUri.extractVideoResolutionMetadata(context: Context): Pair<Int, Int>{
    val metaRetriever = MediaMetadataRetriever()
    metaRetriever.setDataSource(context, this.uri)
    val originalHeight = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
    val originalWidth = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
    metaRetriever.release()

    return Pair(originalWidth, originalHeight)
}
