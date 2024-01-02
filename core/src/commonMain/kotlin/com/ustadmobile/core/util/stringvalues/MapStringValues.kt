package com.ustadmobile.core.util.stringvalues

import com.ustadmobile.core.util.ext.firstCaseInsensitiveOrNull

class MapStringValues(internal val map: Map<String, List<String>>) : IStringValues{
    override fun get(key: String): String? {
        return map.firstCaseInsensitiveOrNull(key)
    }

    override fun getAll(key: String): List<String> {
        return map.entries.filter { it.key.equals(key, true) }.flatMap { it.value }
    }

    override fun names(): Set<String> {
        return map.keys
    }

}

fun Map<String, List<String>>.asIStringValues() = MapStringValues(this)

