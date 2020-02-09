package com.ustadmobile.core.networkmanager

import com.squareup.picasso.Downloader
import okhttp3.*
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * OKHttp does not work on any version of Android lower than Android 5.0 . We therefor need to use
 * an HttpUrlConnection to implement a Picasso Downloader for any version of Android less than 5.0
 */
class PicassoUrlConnectionDownloader: Downloader {

    override fun shutdown() {

    }

    override fun load(request: Request): Response {
        var httpUrlCon: HttpURLConnection? = null
        var httpUrlInputStream: InputStream? = null

        try {
            httpUrlCon = URL(request.url().toString()).openConnection() as HttpURLConnection
            if(httpUrlCon.responseCode != 200)
                throw IOException("PicassoUrlConnectionDownloader: Response code != 200")

            val responseType = httpUrlCon.getHeaderField("Content-Type")
            httpUrlInputStream = httpUrlCon.getInputStream()
            val responseBytes = httpUrlInputStream.readBytes()

            val mediaType = MediaType.get(responseType)
            val responseBuilder = Response.Builder()
                    .code(200)
                    .protocol(Protocol.HTTP_1_1)
                    .message("OK")
                    .request(request)
                    .body(ResponseBody.create(mediaType, responseBytes))

            httpUrlCon.headerFields.filter{it.key != null}.forEach {
                responseBuilder.addHeader(it.key, it.value.first())
            }

            val mainResponse = responseBuilder.build()
            return mainResponse
        }catch(e: Exception) {
            return Response.Builder()
                    .code(404)
                    .request(request)
                    .body(ResponseBody.create(MediaType.get("text/plain"), "Not Found or Error"))
                    .addHeader("Content-Type", "text/plain")
                    .build()
        }finally {
            try{
                httpUrlInputStream?.close()
                httpUrlCon?.disconnect()
            }finally {
                //do nothing else
            }

        }
    }
}