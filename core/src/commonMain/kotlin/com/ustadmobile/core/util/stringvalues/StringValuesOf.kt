package com.ustadmobile.core.util.stringvalues

/**
 * Create IStringValues for map list
 */
fun stringValuesOf(
    vararg pairs: Pair<String, List<String>>,
): IStringValues {
    val map: Map<String, List<String>> = mapOf(*pairs)
    return map.asIStringValues()
}