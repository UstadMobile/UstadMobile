package com.ustadmobile.libcache

/**
 * Split a parameter string up e.g. a cache-control header such as
 * must-revalidate; max-age=600;
 */
fun String.paramTokens(): List<Pair<String, String>> {
    return split(";").map {
        val equalPos = it.indexOf('=')
        if(equalPos > 0 && equalPos < it.length - 1) {
            it.substring(0, equalPos).trim() to it.substring(equalPos + 1).trim()
        }else {
            it.trim() to ""
        }
    }
}
