package com.ustadmobile.ihttp.headers

class MergedHeaders(
    private vararg val sources: IHttpHeaders
) : IHttpHeaders {

    /**
     * Returns the first non-null value available from sources, in order, if any
     */
    override fun get(name: String): String? {
        return sources.firstNotNullOfOrNull { it[name] }
    }

    /**
     * Returns the first non-empty set of headers, if any
     */
    override fun getAllByName(name: String): List<String> {
        return sources.firstNotNullOfOrNull { allByName ->
            allByName.getAllByName(name).let { it.ifEmpty { null } }
        } ?: emptyList()
    }

    override fun names(): Set<String> {
        return sources.flatMap { it.names() }.map { it.lowercase() }.toSet()
    }
}