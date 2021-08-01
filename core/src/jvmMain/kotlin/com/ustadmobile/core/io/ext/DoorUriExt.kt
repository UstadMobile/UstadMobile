package com.ustadmobile.core.io.ext

import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.door.ext.writeToFile
import io.ktor.client.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import java.nio.file.Files
import java.nio.file.Paths
import okhttp3.Request
import java.lang.IllegalStateException

actual suspend fun DoorUri.guessMimeType(di: DI?): String? {
    return if(isRemote()){
        withContext(Dispatchers.IO){
            val okHttpClient: OkHttpClient = di?.direct?.instance() ?: throw IllegalStateException("need di")
            val okRequest = Request.Builder().url(uri.toString()).head().build()
            val response = okHttpClient.newCall(okRequest).execute()
            response.header("Content-Type")
        }
    }else{
        Files.probeContentType(Paths.get(this.uri))
    }
}

actual suspend fun DoorUri.getSize(context: Any, di: DI?): Long {
    return if(isRemote()){
        withContext(Dispatchers.IO){
            val okHttpClient: OkHttpClient = di?.direct?.instance() ?: throw IllegalStateException("need di")
            val okRequest = Request.Builder().url(uri.toString()).head().build()
            val response = okHttpClient.newCall(okRequest).execute()
            val length = response.header("Content-Length")
            length?.toLong() ?: -1
        }
    }else{
        toFile().length()
    }
}

actual suspend fun DoorUri.downloadUrl(tmpDirUri: DoorUri, destination: DoorUri, di: DI, progressListener: (Int) -> Unit) {
    if(isRemote()){
        withContext(Dispatchers.IO){
            val okHttpClient: OkHttpClient = di.direct.instance()
            val okRequest = Request.Builder().url(uri.toString()).build()
            val response = okHttpClient.newCall(okRequest).execute()
            response.body?.byteStream()?.writeToFile(destination.toFile())
        }
    }
}