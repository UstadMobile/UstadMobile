package com.ustadmobile.core.util

import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.logging.NapierLoggingAdapter
import com.ustadmobile.libcache.okhttp.UstadCacheInterceptor
import okhttp3.OkHttpClient
import org.junit.rules.TemporaryFolder
import java.io.File

fun newTestOkHttpClient(
    temporaryFolder: TemporaryFolder,
    cache: UstadCache? = null,
) : OkHttpClient {
    return if(cache != null) {
        OkHttpClient.Builder()
            .addInterceptor(
                UstadCacheInterceptor(
                    cache = cache,
                    tmpDir = File(temporaryFolder.newFolder(), "okhttp-tmp"),
                    logger = NapierLoggingAdapter(),
                )
            )
            .build()
    }else {
        OkHttpClient.Builder().build()
    }
}