package com.ustadmobile.util

data class MatchGroup(val valid: Boolean, val type: String?, val subType: String?)

/**
 * It is not possible to filter out files to select on drag-drop way of selecting
 * files. So, this will try to filter out all selected files which their mimetypes
 * doesn't match a list of accepted mimetypes.
 *
 * i.e Click to browse files apply mimetype/extension filter automatically.
 */
class MimeTypeMatcher(private val mimeTypesToMatch: List<String>){

    private var validatedMimeTypes: List<MatchGroup>

    init {
        validatedMimeTypes = mimeTypesToMatch.map {
            parse(it)
        }.toList()
    }

    /**
     * Regex below validates a selected file mimetype before trying to match from provided mimetypes
     */
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

    /**
     * Match a mimetype/extension from a list of provided mimetypes/extensions,
     * Return TRUE if a selected file mimetype does exist in a list of accepted
     * mimetypes otherwise FALSE
     * i.e if 'image/\*' is in a list then it will match image/jpeg, image/png etc.
     */
    fun match(extOrMimeType: String?): Boolean {
        val actualMatchGroup = parse(extOrMimeType)
        val match = validatedMimeTypes.firstOrNull {
            validateMimeType(actualMatchGroup.type, it.type)
                && validateMimeType(actualMatchGroup.subType, it.subType)
        }
        return match != null || mimeTypesToMatch.indexOf(extOrMimeType) != -1
    }
}

