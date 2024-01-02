package com.ustadmobile.libcache.headers

internal class MapHttpHeadersAdapter(
    private val headersMap: Map<String, List<String>>
) : HttpHeaders{

    override fun get(name: String): String? {
        return headersMap.entries.firstOrNull {
            it.key.equals(name, true)
        }?.value?.firstOrNull()
    }

    override fun getAllByName(name: String): List<String> {
        return headersMap.entries.firstOrNull { it.key.equals(name, true) }?.value
            ?: emptyList()
    }

    override fun names(): Set<String> {
        return headersMap.keys
    }
}