package com.ustadmobile.core.contentformats.har

import com.ustadmobile.core.contentformats.har.HarInterceptor.Companion.interceptorMap
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.io.ext.getStringFromContainerEntry
import com.ustadmobile.core.util.ext.isTextContent
import com.ustadmobile.lib.db.entities.ContainerEntryFile
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.lib.util.parseRangeRequestHeader
import io.ktor.client.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


class HarContainer(val containerUid: Long, val entry: ContentEntry,
                   val umAccount: UmAccount?, val db: UmAppDatabase,
                   val context: Any, val localHttp: String,
                   val httpClient: HttpClient,
                   var block: (sourceUrl: String) -> Unit) {

    lateinit var startingUrl: String
    private val linkPatterns = mutableMapOf<Regex, String>()
    var regexList: List<HarRegexPair>? = null
    var requestMap = mutableMapOf<Pair<String, String>, MutableList<HarEntry>>()
    var interceptors: MutableMap<HarInterceptor, String?> = mutableMapOf()
    val startingUrlDeferred = CompletableDeferred<String>()

    val json = Json { ignoreUnknownKeys = true }

    init {

        val indexEntry =  db.containerEntryDao.findByPathInContainer(containerUid, "harcontent")
        val harExtraEntry = db.containerEntryDao.findByPathInContainer(containerUid, "harextras.json")

        if (indexEntry == null) {
            throw Exception()
        }

        GlobalScope.launch(Dispatchers.Main){

            val harContentData = indexEntry.containerEntryFile?.getStringFromContainerEntry() ?: throw Exception()

            var harExtra = HarExtra()
            if(harExtraEntry != null){
                val data = harExtraEntry.containerEntryFile?.getStringFromContainerEntry() ?: throw Exception()
                harExtra = json.decodeFromString(HarExtra.serializer(), data)
            }

            regexList = harExtra.regexes
            harExtra.links?.forEach {
                linkPatterns[Regex(it.regex)] = it.replacement
            }

            interceptors[RecorderInterceptor()] = null
            interceptors[KhanProgressTracker(httpClient)] = null
            harExtra.interceptors?.forEach {
                val key = interceptorMap[it.name] ?: return@forEach
                interceptors[key] = it.jsonArgs
            }


            val harContent = json.decodeFromString(Har.serializer(), harContentData)

            val entries = harContent.log.entries

            entries.forEach {

                val requestMethod = it.request?.method ?: return@forEach
                val requestUrl = it.request?.url ?: return@forEach

                val pair = Pair(requestMethod, requestUrl)
                if (requestMap.containsKey(pair)) {
                    val list = requestMap.getValue(pair)
                    list.add(it)
                } else {
                    requestMap[pair] = mutableListOf(it)
                }
            }

            startingUrl = entries[0].request?.url ?: "" // TODO throw error message for dialog
            startingUrlDeferred.complete(startingUrl)
        }


    }


    suspend fun serve(request: HarRequest): HarResponse {
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

    private suspend fun getInitialResponse(request: HarRequest): HarResponse {
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
        val containerEntry = db.containerEntryDao.findByPathInContainer(containerUid, harText ?: "")

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

        val harContent = harResponse.content ?: defaultHarContent
        harContent.entryFile = entryFile

        if(harContent.isTextContent()){
            harContent.text = entryFile.getStringFromContainerEntry()
        }

        val rangeHeader: String? = mutMap["Range"] ?: return harResponse

        val totalLength = entryFile.ceTotalSize
        val isHEADRequest = request.method == "HEAD"

        val range = if (rangeHeader != null) {
            parseRangeRequestHeader(rangeHeader, totalLength)
        } else {
            null
        }
        if (range != null && range.statusCode == 206) {

            range.responseHeaders.forEach { mutMap[it.key] = it.value }

            harResponse.status = 206
            harResponse.statusText = "Partial Content"
            harResponse.headers = mutMap.map { HarNameValuePair(it.key, it.value) }
            harContent.entryFile = if (isHEADRequest) null else entryFile
            harContent.text =  if (isHEADRequest) null else harContent.text
            harResponse.content = harContent

            return harResponse

        } else if (range?.statusCode == 416) {

            harResponse.status = 416
            harResponse.statusText = if (isHEADRequest) "" else "Range request not satisfiable"
            harContent.entryFile = null
            harContent.text = null
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
