package com.ustadmobile.util

data class MatchGroup(val valid: Boolean, val type: String?, val subType: String?)

class MimeTypeMatcher(private val mimeTypesToMatch: List<String>){

    private var validatedMimeTypes: List<MatchGroup>

    init {
        validatedMimeTypes = mimeTypesToMatch.map {
            parse(it)
        }.toList()
    }

    private fun parse(mimetype: String?): MatchGroup {
        val regex = "(\\S+|\\*|\\.S)/(\\S+|\\*|\\S-\\S)(\\s*;\\s*(\\w+)=\\s*=\\s*(\\S+))?".toRegex()
        val matches = mimetype?.matches(regex) ?: false
        val matchGroups = regex.matchEntire(mimetype ?: "")?.groups
        return MatchGroup(matches, matchGroups?.get(1)?.value, matchGroups?.get(2)?.value)
    }

    private fun validateMimeType(actual: String?, expected: String?): Boolean {
        return if(expected == "*") true
        else expected?.lowercase() == actual?.lowercase()
    }

    fun match(extOrMimeType: String?): Boolean {
        val actualMatchGroup = parse(extOrMimeType)
        val match = validatedMimeTypes.firstOrNull {
            validateMimeType(actualMatchGroup.type, it.type)
                && validateMimeType(actualMatchGroup.subType, it.subType)
        }
        return match != null || mimeTypesToMatch.indexOf(extOrMimeType) != -1
    }
}

