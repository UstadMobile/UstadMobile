package com.ustadmobile.ihttp.headers.directives

/**
 * Parse a header into a map of directives to value. If the directive has an '=',
 * then the portion after the '=' will be the value in the map. If there is nothing after the equals
 * sign, then the value in the map will be a empty string ("").
 *
 * @param header Cache-Control header (value only)
 * @return map of the directives to value (if any)
 */
fun directivesToMap(header: String): Map<String, String> {
    val directives = header.split(",").map { it.lowercase().trim() }
    return directives.map { directive ->
        val directiveSplit = directive.split("=", limit = 2)
        if(directiveSplit.size == 1) {
            Pair(directive, "")
        }else {
            Pair(directiveSplit[0], directiveSplit[1])
        }
    }.toMap()
}
