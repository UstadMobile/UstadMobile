package com.ustadmobile.core.io.ext

import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile
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
import okhttp3.Response
import okhttp3.internal.closeQuietly
import java.io.IOException

actual suspend fun DoorUri.guessMimeType(context: Any, di: DI): String? {
    return if(isRemote()){
        withContext(Dispatchers.IO){
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
    }else{
        Files.probeContentType(Paths.get(this.uri))
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
    }else{
        try {
            toFile().length()
        }catch (e: Exception){
            -1
        }
    }
}