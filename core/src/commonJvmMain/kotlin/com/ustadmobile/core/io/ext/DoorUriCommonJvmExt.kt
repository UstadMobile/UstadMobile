package com.ustadmobile.core.io.ext

import com.ustadmobile.door.DoorUri
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.closeQuietly
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import com.ustadmobile.door.ext.toFile


actual suspend fun DoorUri.isRemote(): Boolean {
    val prefix = this.uri.toString().substringBefore("//").lowercase()
    return prefix.startsWith("http:") || prefix.startsWith("https:")
}

actual suspend fun DoorUri.downloadUrlIfRemote(destination: DoorUri, di: DI) {
    if(isRemote()){
        withContext(Dispatchers.IO){
            var response: Response? = null
            try {
                val okHttpClient: OkHttpClient = di.direct.instance()
                val okRequest = okhttp3.Request.Builder().url(uri.toString()).build()
                response = okHttpClient.newCall(okRequest).execute()
                response.body?.byteStream()?.writeToFileAsync(destination.toFile())
            }catch (io: IOException){
                throw io
            }catch (e: Exception) {
                return@withContext null
            }finally {
                response?.closeQuietly()
            }
        }
    }
}


actual suspend fun DoorUri.emptyRecursively() {
    withContext(Dispatchers.IO) {
        toFile().listFiles()?.forEach {
            it.deleteRecursively()
        }
    }
}

actual suspend fun DoorUri.deleteRecursively() {
    withContext(Dispatchers.IO) {
        toFile().deleteRecursively()
    }
}
