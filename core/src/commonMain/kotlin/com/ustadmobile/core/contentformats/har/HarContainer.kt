package com.ustadmobile.core.contentformats.har

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.io.RangeInputStream
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.lib.util.parseRangeRequestHeader
import io.ktor.client.request.request
import kotlinx.serialization.Serializable
import kotlinx.serialization.internal.HashMapSerializer
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list

class HarContainer(val containerManager: ContainerManager, var block: (sourceUrl: String) -> Unit) {

    var startingUrl: String
    private val linkPatterns = mutableMapOf<Regex, String>()
    var regexList: List<HarRegexPair>? = null
    var requestMap = mutableMapOf<Pair<String, String>, MutableList<HarEntry>>()

    init {

        val indexEntry = containerManager.getEntry("harcontent")
        val harExtraEntry = containerManager.getEntry("harextras.json")

        if (indexEntry == null) {
            throw Exception()
        }

        val json = Json(JsonConfiguration(
                encodeDefaults = true,
                strictMode = false,
                unquoted = false,
                allowStructuredMapKeys = true,
                prettyPrint = false,
                useArrayPolymorphism = false
        ))

        var harExtra = HarExtra()
        if (harExtraEntry != null) {
            harExtra = json.parse(HarExtra.serializer(), UMIOUtils.readToString(containerManager.getInputStream(harExtraEntry)))
        }

        regexList = harExtra.regexes
        harExtra.links?.forEach {
            linkPatterns[Regex(it.regex)] = it.replacement
        }


        val harContent = json.parse(Har.serializer(), UMIOUtils.readToString(containerManager.getInputStream(indexEntry)))

        val entries = harContent.log.entries


        val found = entries.find { it.request!!.url!!.contains("3/ass") }

        entries.forEach {

            val pair = Pair(it.request!!.method!!, it.request.url!!)
            if(requestMap.containsKey(pair)){
                val list = requestMap[pair]
                list!!.add(it)
            }else{
                requestMap[pair] = mutableListOf(it)
            }
        }

        startingUrl = entries[0].request?.url ?: ""

    }


    fun serve(request: HarRequest): HarResponse {
        val url = request.url!!
        print(url)
        var regexedUrl = url
        regexList?.forEach { itRegex ->
            regexedUrl = regexedUrl.replace(Regex(itRegex.regex), itRegex.replacement)
        }

        checkWithPattern(regexedUrl)

        val harList = requestMap[(Pair(request.method!!, regexedUrl))]

        val harResponse = HarResponse()
        val harContent = HarContent()

        if (harList == null) {
            harResponse.status = 401
            harResponse.statusText = "OK"
            harContent.mimeType = ""
            harResponse.content = harContent

            return harResponse
        }

        if(harList.size > 1){
            println(regexedUrl)
        }

        val harEntry = harList[0]

        val containerEntry = containerManager.getEntry(harEntry.response!!.content!!.text!!)

        if (containerEntry == null) {
            harResponse.status = 402
            harResponse.statusText = "Not Found"

            harResponse.content = harContent

            return harResponse
        }

        val mutMap = mutableMapOf<String, String>()
        mutMap.putAll(harEntry.response.headers.map { it.name!! to it.value!! }.toMap())
        if (containerEntry.containerEntryFile!!.compression == ContainerEntryFile.COMPRESSION_GZIP) {
            mutMap["Content-Encoding"] = "gzip"
            mutMap["Content-Length"] = containerEntry.containerEntryFile!!.ceCompressedSize.toString()
        }

        if (!mutMap.containsKey("access-control-allow-origin")) {
            mutMap["Access-Control-Allow-Origin"] = "*"
        }
        mutMap["Access-Control-Allow-Headers"] = "X-Requested-With"

        harEntry.response.headers = mutMap.map { HarNameValuePair(it.key, it.value) }

        if (request.method == "OPTIONS") {
            return harEntry.response
        }

        var data = containerManager.getInputStream(containerEntry)

        harEntry.response.content!!.data = data

        val rangeHeader: String? = mutMap["Range"] ?: return harEntry.response

        val totalLength = containerEntry.containerEntryFile!!.ceTotalSize
        val isHEADRequest = request.method == "HEAD"

        val range = if (rangeHeader != null) {
            parseRangeRequestHeader(rangeHeader, totalLength)
        } else {
            null
        }
        if (range != null && range.statusCode == 206) {
            if (!isHEADRequest) {
                data = RangeInputStream(data, range.fromByte, range.toByte)
            }

            range.responseHeaders.forEach { mutMap[it.key] = it.value }

            harEntry.response.status = 206
            harResponse.statusText = "Partial Content"
            harEntry.response.headers = mutMap.map { HarNameValuePair(it.key, it.value) }
            harEntry.response.content!!.data = if (isHEADRequest) null else data

            return harEntry.response

        } else if (range?.statusCode == 416) {

            harEntry.response.status = 416
            harResponse.statusText = if (isHEADRequest) "" else "Range request not satisfiable"
            harEntry.response.content!!.data = null

            return harResponse
        } else {

            mutMap["Content-Length"] = totalLength.toString()
            mutMap["Connection"] = "close"

            harEntry.response.headers = mutMap.map { HarNameValuePair(it.key, it.value) }

            return harEntry.response
        }
    }

    fun checkWithPattern(requestUrl: String) {
        for (linkPattern in linkPatterns.keys) {
            if (linkPattern.matches(requestUrl)) {
                val pattern = linkPatterns[linkPattern]
                if (pattern != null) {
                    block.invoke(pattern)
                }
            }
        }
    }


}