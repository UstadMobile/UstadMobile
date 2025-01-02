package com.ustadmobile.libcache.okhttp

import com.ustadmobile.ihttp.okhttp.request.asIHttpRequest
import com.ustadmobile.ihttp.okhttp.request.asOkHttpRequest
import com.ustadmobile.libcache.distributed.DistributedCacheConstants.DCACHE_LOGTAG
import com.ustadmobile.libcache.distributed.DistributedCacheHashtable
import com.ustadmobile.libcache.logging.UstadCacheLogger
import okhttp3.Interceptor
import okhttp3.Response

class DistributedCacheInterceptor(
    val distributedCacheHashtable: DistributedCacheHashtable,
    private val logger: UstadCacheLogger,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val chainRequest = chain.request()
        val localRequest = distributedCacheHashtable.localRequestFor(chainRequest.asIHttpRequest())?.also {
            logger.i(DCACHE_LOGTAG, "Local Download: ${chainRequest.url} from ${it.url}")
        }

        //here: could monitor the local response e.g. to track success/fail per node
        return chain.proceed(localRequest?.asOkHttpRequest() ?: chainRequest)
    }
}
