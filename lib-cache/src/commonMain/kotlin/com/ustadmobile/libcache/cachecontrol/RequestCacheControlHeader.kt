package com.ustadmobile.libcache.cachecontrol

/**
 * Request Cache Control headers (includes only those that are valid for request headers).
 *
 * See:
 *  https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control
 */
@Suppress("unused") //Props are as per the spec
class RequestCacheControlHeader(
    val maxAge: Long?,
    val maxStale: Long?,
    val minFresh: Long?,
    val noCache: Boolean,
    val noStore: Boolean,
    val noTransform: Boolean,
    val onlyIfCached: Boolean,
    val staleIfError: Long?,
) {

    companion object {

        fun parse(header: String): RequestCacheControlHeader {
            val directivesMap = headerDirectivesToMap(header)

            val maxAge = directivesMap["max-age"]?.toLong()
            val maxStale = directivesMap["max-stale"]?.toLong()
            val minFresh = directivesMap["min-fresh"]?.toLong()
            val noCache = directivesMap.containsKey("no-cache")
            val noStore = directivesMap.containsKey("no-store")
            val noTransform = directivesMap.containsKey("no-transform")
            val onlyIfCached = directivesMap.containsKey("only-if-cached")
            val staleIfError = directivesMap["stale-if-error"]?.toLong()

            return RequestCacheControlHeader(
                maxAge = maxAge,
                maxStale = maxStale,
                minFresh = minFresh,
                noCache = noCache,
                noStore = noStore,
                noTransform = noTransform,
                onlyIfCached = onlyIfCached,
                staleIfError = staleIfError,
            )
        }
    }

}