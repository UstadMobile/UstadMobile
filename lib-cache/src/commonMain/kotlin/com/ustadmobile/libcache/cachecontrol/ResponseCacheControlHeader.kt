package com.ustadmobile.libcache.cachecontrol

/**
 * Response Cache Control headers (includes only those that are valid on the response)
 */
@Suppress("unused") //Props are as per the spec
class ResponseCacheControlHeader(
    val maxAge: Long?,
    val sMaxAge: Long?,
    val noCache: Boolean,
    val noStore: Boolean,
    val noTransform: Boolean,
    val mustRevalidate: Boolean,
    val proxyRevalidate: Boolean,
    val mustUnderstand: Boolean,
    val isPrivate: Boolean,
    val isPublic: Boolean,
    val immutable: Boolean,
    val staleWhileRevalidate: Long?,
    val staleIfError: Long?,
) {

    companion object {

        fun parse(header: String): ResponseCacheControlHeader {
            val directivesMap = headerDirectivesToMap(header)
            return ResponseCacheControlHeader(
                maxAge = directivesMap["max-age"]?.toLong(),
                sMaxAge = directivesMap["s-maxage"]?.toLong(),
                noCache = directivesMap.containsKey("no-cache"),
                noStore = directivesMap.containsKey("no-store"),
                noTransform = directivesMap.containsKey("no-transform"),
                mustRevalidate = directivesMap.containsKey("must-revalidate"),
                proxyRevalidate = directivesMap.containsKey("proxy-revalidate"),
                mustUnderstand = directivesMap.containsKey("must-understand"),
                isPrivate = directivesMap.containsKey("private"),
                isPublic = directivesMap.containsKey("public"),
                immutable = directivesMap.containsKey("immutable"),
                staleWhileRevalidate = directivesMap["stale-while-revalidate"]?.toLong(),
                staleIfError = directivesMap["stale-if-error"]?.toLong(),
            )
        }

    }

}