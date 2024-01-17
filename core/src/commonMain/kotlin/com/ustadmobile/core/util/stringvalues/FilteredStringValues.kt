package com.ustadmobile.core.util.stringvalues

class FilteredStringValues(
    private val srcValues: IStringValues,
    private val filter: (String) -> Boolean
) : IStringValues {
    override fun get(key: String): String? {
        return if(filter(key)) srcValues[key] else null
    }

    override fun getAll(key: String): List<String> {
        return if(filter(key)) srcValues.getAll(key) else emptyList()
    }

    override fun names(): Set<String> {
        return srcValues.names().filter(filter).toSet()
    }
}

fun IStringValues.filtered(
    predicate: (String) -> Boolean
): IStringValues = FilteredStringValues(this, predicate)
