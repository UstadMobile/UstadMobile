package com.ustadmobile.core.util.stringvalues

class OverrideValues(
    private val srcValues: IStringValues,
    private val overrides: IStringValues
) : IStringValues {

    override fun get(key: String): String? {
        return overrides.get(key) ?: srcValues.get(key)
    }

    override fun getAll(key: String): List<String> {
        val overrideVal = overrides.getAll(key)
        return if(overrideVal.isNotEmpty())
            overrideVal
        else
            srcValues.getAll(key)
    }

    override fun names(): Set<String> {
        val allNames = overrides.names().map { it.lowercase() } +
                srcValues.names().map { it.lowercase() }
        return allNames.toSet()
    }
}

fun IStringValues.withOverrides(
    overrides: IStringValues
): IStringValues = OverrideValues(srcValues = this, overrides = overrides)
