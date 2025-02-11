package com.ustadmobile.test.http

import okhttp3.OkHttpClient
import okhttp3.Request

fun OkHttpClient.waitForUrl(
    url: String,
    requestTimeout: Long = 1_000,
    totalTimeout: Long = 6_000,
    interval: Long = 500,
) {
    val timeNow = System.currentTimeMillis()

    do {
        try {
            newCall(Request.Builder().url(url).build()).execute().use { response ->
                //For some reason a response code 302 is not considered successful
                if(response.code == 302 || response.isSuccessful) {
                    System.out.println("$url ready")
                    return
                }
            }
        }catch(e: Throwable) {
            Thread.sleep(interval)
        }
    } while(System.currentTimeMillis() - timeNow < totalTimeout)

    throw IllegalStateException("url $url not ready after $totalTimeout ms")
}
