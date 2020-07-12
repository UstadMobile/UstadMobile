package com.ustadmobile.core.contentformats.har


import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.contentformats.har.HarInterceptor.Companion.interceptorMap
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.RangeInputStream
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.parseRangeRequestHeader
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.JsonConfiguration


@ExperimentalStdlibApi
class HarContainer(val containerManager: ContainerManager, val entry: ContentEntry,
                   val umAccount: UmAccount?, val context: Any,
                   val localHttp: String, var block: (sourceUrl: String) -> Unit) {

    var startingUrl: String
    private val linkPatterns = mutableMapOf<Regex, String>()
    var regexList: List<HarRegexPair>? = null
    var requestMap = mutableMapOf<Pair<String, String>, MutableList<HarEntry>>()
    var interceptors: MutableMap<HarInterceptor, String?> = mutableMapOf()

    val json = Json(JsonConfiguration(ignoreUnknownKeys = true))

    init {

        val indexEntry = containerManager.getEntry("harcontent")
        val harExtraEntry = containerManager.getEntry("harextras.json")

        if (indexEntry == null) {
            throw Exception()
        }

        var harExtra = HarExtra()
        if (harExtraEntry != null) {
            val data = UMIOUtils.readStreamToString(containerManager.getInputStream(harExtraEntry))
            harExtra = json.parse(HarExtra.serializer(), data)
        }

        regexList = harExtra.regexes
        harExtra.links?.forEach {
            linkPatterns[Regex(it.regex)] = it.replacement
        }

        interceptors[RecorderInterceptor()] = null
        interceptors[KhanProgressTracker()] = null
        harExtra.interceptors?.forEach {
            val key = interceptorMap[it.name] ?: return@forEach
            interceptors[key] = it.jsonArgs
        }


        val harContent = json.parse(Har.serializer(), UMIOUtils.readStreamToString(containerManager.getInputStream(indexEntry)))

        val entries = harContent.log.entries

        entries.forEach {

            val requestMethod = it.request?.method ?: return@forEach
            val requestUrl = it.request.url ?: return@forEach

            val pair = Pair(requestMethod, requestUrl)
            if (requestMap.containsKey(pair)) {
                val list = requestMap.getValue(pair)
                list.add(it)
            } else {
                requestMap[pair] = mutableListOf(it)
            }
        }

        startingUrl = entries[0].request?.url ?: "" // TODO throw error message for dialog

    }


    fun serve(request: HarRequest): HarResponse {
        var regexedUrl = request.url ?: ""
        regexList?.forEach { itRegex ->
            regexedUrl = regexedUrl.replace(Regex(itRegex.regex), itRegex.replacement)
        }

        request.regexedUrl = regexedUrl

        checkWithPattern(regexedUrl)

        var response = getInitialResponse(request)
        interceptors.forEach {
            response = it.key.intercept(request, response, this, it.value)
        }

        return response
    }

    private fun getInitialResponse(request: HarRequest): HarResponse {
        val harList = requestMap[(Pair(request.method, request.regexedUrl))]

        val defaultResponse = HarResponse()
        val defaultHarContent = HarContent()

        if (harList.isNullOrEmpty()) {
            defaultResponse.status = 404
            defaultResponse.statusText = "OK"
            defaultHarContent.mimeType = ""
            defaultResponse.content = defaultHarContent

            return defaultResponse
        }

        val harEntry = harList[0]

        val harResponse = harEntry.response ?: defaultResponse
        val harText = harResponse.content?.text
        val containerEntry = containerManager.getEntry(harText ?: "")

        val entryFile = containerEntry?.containerEntryFile
        if (entryFile == null) {
            harResponse.status = 402
            harResponse.statusText = "Not Found"

            harResponse.content = defaultHarContent
            return harResponse
        }

        val mutMap = getHeaderMap(harResponse.headers, entryFile)
        harResponse.headers = mutMap.map { HarNameValuePair(it.key, it.value) }

        if (request.method == "OPTIONS") {
            return harResponse
        }

        var data = containerManager.getInputStream(containerEntry)

        val harContent = harResponse.content ?: defaultHarContent
        harContent.data = data

        val rangeHeader: String? = mutMap["Range"] ?: return harResponse

        val totalLength = entryFile.ceTotalSize
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

            harResponse.status = 206
            harResponse.statusText = "Partial Content"
            harResponse.headers = mutMap.map { HarNameValuePair(it.key, it.value) }
            harContent.data = if (isHEADRequest) null else data
            harResponse.content = harContent

            return harResponse

        } else if (range?.statusCode == 416) {

            harResponse.status = 416
            harResponse.statusText = if (isHEADRequest) "" else "Range request not satisfiable"
            harContent.data = null
            harResponse.content = harContent

            return harResponse
        } else {

            mutMap["Content-Length"] = totalLength.toString()
            mutMap["Connection"] = "close"

            harResponse.headers = mutMap.map { HarNameValuePair(it.key, it.value) }

            return harResponse
        }
    }

    fun getHeaderMap(harHeaders: List<HarNameValuePair>, containerEntryFile: ContainerEntryFile): MutableMap<String, String> {
        val mutMap = mutableMapOf<String, String>()
        mutMap.putAll(harHeaders.map { it.name to it.value }.toMap())
        if (containerEntryFile.compression == ContainerEntryFile.COMPRESSION_GZIP) {
            mutMap["Content-Encoding"] = "gzip"
            mutMap["Content-Length"] = containerEntryFile.ceCompressedSize.toString()
        }

        if (!mutMap.containsKey("access-control-allow-origin")) {
            mutMap["Access-Control-Allow-Origin"] = "*"
        }
        mutMap["Access-Control-Allow-Headers"] = "X-Requested-With"

        return mutMap
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
