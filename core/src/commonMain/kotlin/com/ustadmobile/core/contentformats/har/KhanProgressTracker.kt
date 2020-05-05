package com.ustadmobile.core.contentformats.har

import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.tincan.UmAccountActor
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.core.util.UMTinCanUtil
import com.ustadmobile.core.util.ext.toXapiActorJsonObject
import io.ktor.client.call.receive
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.response.HttpResponse
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.takeFrom
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.parse
import kotlinx.serialization.stringify

@Serializable
class ItemResponse {

    var itemData: String? = null

}

@Serializable
class ItemData {

    var question: Content? = null

    @Serializable
    inner class Content {

        var content: String? = null
    }

}


class KhanProgressTracker : HarInterceptor() {

    val exercisePath = "/api/internal/user/exercises/"
    val itemPath = "/items/"
    val langPath = "/assessment_item?lang="

    var counter = 1
    val client =  defaultHttpClient()

    override fun intercept(request: HarRequest, response: HarResponse, harContainer: HarContainer, jsonArgs: String?): HarResponse {

        if(harContainer.umAccount == null){
            return response
        }

        if (request.regexedUrl?.contains("khanacademy.org") == false || request.regexedUrl?.contains("attempt") == false) {
            return response
        }

        var lang = request.url?.substringBefore(".khan")?.substringAfter("://") ?: "en"
        if (lang == "www") {
            lang = "en"
        }

        val json = harContainer.json

        val attemptBody = request.body ?: return response
        val body = json.parse(KhanProblemBody.serializer(), attemptBody)
        val bodyInput = body.variables?.input ?: return response

        // build url to get question content
        val requestUrl = Url(request.url ?: "")
        val sourceUrl = harContainer.entry.sourceUrl ?: ""
        val exerciseId = sourceUrl.substringAfter("khan-id://").substringBefore(".")
        val finalUrl = URLBuilder(protocol = URLProtocol.HTTPS, host = requestUrl.host, encodedPath = "$exercisePath$exerciseId$itemPath${bodyInput.assessmentItemId}$langPath$lang").buildString()

        val harList = harContainer.requestMap[(Pair("GET", finalUrl))]

        if (harList.isNullOrEmpty()) return response

        val resultEntry = harList[0]
        val harText = resultEntry.response?.content?.text
        val containerEntry = harContainer.containerManager.getEntry(harText
                ?: "") ?: return response
        val data = harContainer.containerManager.getInputStream(containerEntry)
        val result = UMIOUtils.readToString(data)
        val itemResp = json.parse(ItemResponse.serializer(), result).itemData ?: return response
        var question = json.parse(ItemData.serializer(), itemResp).question?.content ?: return response

        question = question.replace(Regex("(\\[\\[(.*)]])|\\*|\\n|:-: \\||\\{|\\}|\\\$large"),"")

        val skipped = bodyInput.skipped
        val completed = if(skipped) false else bodyInput.completed
        val timeTaken = UMTinCanUtil.format8601Duration(bodyInput.timeTaken)
        val actor = harContainer.json.stringify(UmAccountActor.serializer(), harContainer.umAccount.toXapiActorJsonObject(harContainer.context))
        val verbUrl = if(skipped) "http://id.tincanapi.com/verb/skipped" else "http://adlnet.gov/expapi/verbs/answered"
        val verbDisplay = if(skipped) "skipped" else "answered"

        val statement = """

        {
            "actor": $actor,
            "verb": {
                "id": "$verbUrl",
                "display": {
                    "en-US": "$verbDisplay"
                }
            },
            "result": {
                "success" : $completed,
                "duration" : "$timeTaken"
            },
            "object": {
                "id" : "$finalUrl",
                "objectType" : "Activity",
                "definition" : {
                        "name": {"en-US":"Exercise $exerciseId : Question $counter"},
                        "description": {"en-US":"$question"}
                }
            }
        }
            
        """.trimIndent()

        if(bodyInput.completed){
            counter++
        }

        GlobalScope.launch {

            val httpResponse = client.put<HttpResponse>{
                url {
                    takeFrom(harContainer.localHttp)
                    encodedPath = "${encodedPath}xapi/${harContainer.entry.contentEntryUid}/statements"
                }
                this.body = statement
            }
            val status = httpResponse.status
            print(status)

        }


        return response
    }


}