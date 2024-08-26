package com.ustadmobile.ihttp.headers

/**
 * Map http headers as required. This can be used to filter out headers e.g. when a validation
 * response is received that incorrectly specifies a content-length of zero etc.
 */
internal class MappedHttpHeaders(
    private val srcHeaders: IHttpHeaders,
    private val mapFn: (headerName: String, headerValue: String) -> String?
) : IHttpHeaders{

    override fun get(name: String): String? {
        return srcHeaders[name]?.let { mapFn(name, it) }
    }

    override fun getAllByName(name: String): List<String> {
        return srcHeaders.getAllByName(name).mapNotNull {
            mapFn(name, it)
        }
    }

    override fun names(): Set<String> {
        return srcHeaders.names().filter { headerName ->
            val headerVal = srcHeaders[headerName]
            headerVal?.let { mapFn(headerName, it)  } != null
        }.toSet()
    }
}

fun IHttpHeaders.mapHeaders(
    block:  (headerName: String, headerValue: String) -> String?
): IHttpHeaders = MappedHttpHeaders(this, block)
