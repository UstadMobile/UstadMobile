package com.ustadmobile.core.contentformats.har

import kotlinx.serialization.Serializable

@Serializable
data class HarRegexPair(val regex: String, val replacement: String)

@Serializable
data class Interceptors(val name: String, val jsonArgs: String)

@Serializable
class HarExtra {

    var regexes: List<HarRegexPair>? = listOf()

    var links: List<HarRegexPair>? = listOf()

    var interceptors: List<Interceptors>? = listOf()

}