package com.ustadmobile.core.util

import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.logging.NapierLoggingAdapter
import com.ustadmobile.libcache.okhttp.UstadCacheInterceptor
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.junit.rules.TemporaryFolder
import java.io.File

fun newTestOkHttpClient(
    temporaryFolder: TemporaryFolder,
    cache: UstadCache? = null,
    json: Json,
) : OkHttpClient {
    return if(cache != null) {
        OkHttpClient.Builder()
            .addInterceptor(
                UstadCacheInterceptor(
                    cache = cache,
                    tmpDirProvider = { File(temporaryFolder.newFolder(), "okhttp-tmp") },
                    logger = NapierLoggingAdapter(),
                    json = json,
                )
            )
            .build()
    }else {
        OkHttpClient.Builder().build()
    }
}