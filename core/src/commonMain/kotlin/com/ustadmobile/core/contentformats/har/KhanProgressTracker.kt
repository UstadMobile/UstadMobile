package com.ustadmobile.core.contentformats.har

import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.tincan.UmAccountActor
import com.ustadmobile.core.util.MimeType
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.core.util.UMTinCanUtil
import com.ustadmobile.core.util.ext.toXapiActorJsonObject
import io.ktor.client.request.put
import io.ktor.client.response.HttpResponse
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.http.takeFrom
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.io.ByteArrayInputStream
import kotlinx.serialization.Serializable
import kotlinx.serialization.toUtf8Bytes

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
    var numCorrect = 0;
    val client = defaultHttpClient()
    var totalTime = 0L

    override fun intercept(request: HarRequest, response: HarResponse, harContainer: HarContainer, jsonArgs: String?): HarResponse {

        /*if(harContainer.umAccount == null){
            return response
        }*/

        if (request.regexedUrl?.contains("khanacademy.org") == false || (request.regexedUrl?.contains("attempt") == false && request.regexedUrl?.contains("getEotCardDetails") == false)) {
            return response
        }

        var lang = request.url?.substringBefore(".khan")?.substringAfter("://") ?: "en"
        if (lang == "www") {
            lang = "en"
        }

        val json = harContainer.json

        val requestUrl = Url(request.url ?: "")
        val sourceUrl = harContainer.entry.sourceUrl ?: ""
        val exerciseId = sourceUrl.substringAfter("khan-id://").substringBefore(".")
        val urlToFindTotalQuestions = URLBuilder(protocol = URLProtocol.HTTPS, host = requestUrl.host, encodedPath = "$exercisePath$exerciseId").buildString()

        val totalQuestions = harContainer.requestMap.filterKeys { it.second.startsWith(urlToFindTotalQuestions) }.size

        val actor = harContainer.json.stringify(UmAccountActor.serializer(), harContainer.umAccount.toXapiActorJsonObject(harContainer.context))

        if (request.regexedUrl?.contains("getEotCardDetails") == true) {

            val totalTimeFormat = UMTinCanUtil.format8601Duration(totalTime * 1000)

            val completeResponse = """
                
               {
                 "errors": null,
                 "data": {
                   "user": {
                     "id": "kaid_346437629923241310559242",
                     "exerciseData": {
                       "practiceAttempt": {
                         "id": "ag5zfmtoYW4tYWNhZGVteXIZCxIMTGVhcm5pbmdUYXNrGICAuf7P0P8JDA",
                         "numAttempted": $totalQuestions,
                         "numCorrect": $numCorrect,
                         "startingFpmLevel": "unfamiliar",
                         "endingFpmLevel": "familiar",
                         "masteryLevelChange": "UP",
                         "pointsEarned": ${numCorrect * 100},
                         "__typename": "PracticeAttempt"
                       },
                       "__typename": "UserExerciseData"
                     },
                     "__typename": "User"
                   }
                 }
               }

            """.trimIndent()

            val statement = """
                
                {
                    "actor": $actor,
                    "verb": {
                        "id": "http://adlnet.gov/expapi/verbs/completed",
                        "display": {
                            "en-US": "completed"
                        }
                    },
                    "result": {
                        "completion": true,
                        "duration": "$totalTimeFormat"
                    },
                    "object": {
                        "id": "$sourceUrl",
                        "objectType": "Activity",
                        "definition": {
                            "name": {
                                "en-US": "${harContainer.entry.title}"
                            },
                            "description": {
                                "en-US": "${harContainer.entry.description}"
                            }
                        }
                    }
                }
                
                
            """.trimIndent()

            sendStatement(statement, harContainer)

            val cardDetailsContent = HarContent()
            cardDetailsContent.data = ByteArrayInputStream(completeResponse.toUtf8Bytes())
            cardDetailsContent.text = completeResponse
            cardDetailsContent.mimeType = "application/json"
            cardDetailsContent.size = completeResponse.length.toLong()
            cardDetailsContent.encoding = "UTF-8"

            val cardDetailsResponse = HarResponse()
            cardDetailsResponse.content = cardDetailsContent
            cardDetailsResponse.status = 200
            cardDetailsResponse.statusText = "OK"

            return cardDetailsResponse
        }

        val attemptBody = request.body ?: return response
        val body = json.parse(KhanProblemBody.serializer(), attemptBody)
        val bodyInput = body.variables?.input ?: return response

        // build url to get question content
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
        var question = json.parse(ItemData.serializer(), itemResp).question?.content
                ?: return response

        question = question.replace(Regex("(\\[\\[(.*)]])|\\*|\\n|:-: \\||\\{|\\}|\\\$large"), "")

        val skipped = bodyInput.skipped
        val completed = if (skipped) false else bodyInput.completed
        totalTime += bodyInput.timeTaken
        val timeTaken = UMTinCanUtil.format8601Duration(bodyInput.timeTaken * 1000)
        val verbUrl = if (skipped) "http://id.tincanapi.com/verb/skipped" else "http://adlnet.gov/expapi/verbs/answered"
        val verbDisplay = if (skipped) "skipped" else "answered"

        if (bodyInput.countHints == 0 && completed) {
            numCorrect++
        }

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
                "duration" : "$timeTaken",
                 "extensions": {
                      "https://w3id.org/xapi/cmi5/result/extensions/progress": ${((counter.toFloat()) / (totalQuestions) * 100).toInt()}
                    }
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

        if (bodyInput.completed) {
            counter++
        }

        sendStatement(statement, harContainer)


        return response
    }

    private fun sendStatement(statement: String, harContainer: HarContainer) {

        GlobalScope.launch {

            client.put<HttpResponse> {
                url {
                    takeFrom(harContainer.localHttp)
                    encodedPath = "${encodedPath}xapi/${harContainer.entry.contentEntryUid}/statements"
                }
                this.body = statement
            }

        }

    }


}