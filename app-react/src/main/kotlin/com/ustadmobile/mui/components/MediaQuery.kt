package com.ustadmobile.mui.components

import kotlinx.js.jso
import mui.system.Theme
import org.w3c.dom.MediaQueryList

@JsModule("@mui/material/useMediaQuery")
@JsNonModule
private external val useMediaQuery: dynamic

@Suppress("UnsafeCastFromDynamic")
private fun useMediaQueryRaw(query: Any, options: dynamic): Boolean = useMediaQuery.default(query, options)

typealias MatchMedia = ((query: String) -> MediaQueryList)

fun useMediaQuery(
    query: String,
    defaultMatches: Boolean? = null,
    matchMedia: MatchMedia? = null,
    noSsr: Boolean? = null,
    ssrMatchMedia: MatchMedia? = null
): Boolean {
    return useMediaQueryRaw(query, convertOptions(defaultMatches, matchMedia, noSsr, ssrMatchMedia))
}

fun useMediaQuery(
    query: (theme: Theme) -> String,
    defaultMatches: Boolean? = null,
    matchMedia: MatchMedia? = null,
    noSsr: Boolean? = null,
    ssrMatchMedia: MatchMedia? = null
): Boolean {
    return useMediaQueryRaw(query, convertOptions(defaultMatches, matchMedia, noSsr, ssrMatchMedia))
}

private fun convertOptions(
    defaultMatches: Boolean?,
    matchMedia: MatchMedia?,
    noSsr: Boolean?,
    ssrMatchMedia: MatchMedia?
): dynamic = jso {
    defaultMatches?.let {
        this.defaultMatches = defaultMatches
    }
    matchMedia?.let {
        this.matchMedia = matchMedia
    }
    noSsr?.let {
        this.noSsr = noSsr
    }
    ssrMatchMedia?.let {
        this.ssrMatchMedia = ssrMatchMedia
    }
}