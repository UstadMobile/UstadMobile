package com.ustadmobile.core.contentformats.har

import com.ustadmobile.core.io.ext.getStringFromContainerEntry
import com.ustadmobile.core.tincan.UmAccountActor
import com.ustadmobile.core.util.UMTinCanUtil
import com.ustadmobile.core.util.ext.toXapiActorJsonObject
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.HttpStatement
import io.ktor.http.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
class ItemResponse {

    var itemData: String? = null

}

@Serializable
class ItemData {

    var question: Content? = null

    @Serializable
    class Content {

        var content: String? = null
    }

}


class KhanProgressTracker(val httpClient: HttpClient) : HarInterceptor() {

    val exercisePath = "/api/internal/user/exercises/"
    val itemPath = "/items/"
    val langPath = "/assessment_item?lang="

    var counter = 1
    var numCorrect = 0;
    var totalTime = 0L

    override suspend fun intercept(request: HarRequest, response: HarResponse, harContainer: HarContainer, jsonArgs: String?): HarResponse {

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
        val urlToFindTotalQuestions = URLBuilder(protocol = URLProtocol.HTTPS, host = requestUrl.host,
            pathSegments = listOf(exercisePath, exerciseId)).buildString()

        val totalQuestions = harContainer.requestMap.filterKeys { it.second.startsWith(urlToFindTotalQuestions) }.size

        val actor = harContainer.json.encodeToString(UmAccountActor.serializer(),
            harContainer.umAccount.toXapiActorJsonObject(harContainer.context))

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
                        "score": {
                            "scaled": ${(numCorrect.toFloat()) / (totalQuestions)},
                            "raw": $numCorrect,
                            "min": 0,
                            "max": $totalQuestions
                        },
                        "duration": "$totalTimeFormat",
                         "extensions": {
                              "https://w3id.org/xapi/cmi5/result/extensions/progress": 100
                         }
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

            // don't send statement if user not logged in
            if (harContainer.umAccount != null) {
                sendStatement(statement, harContainer)
            }

            val cardDetailsContent = HarContent()
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
        val body = json.decodeFromString(KhanProblemBody.serializer(), attemptBody)
        val bodyInput = body.variables?.input ?: return response

        // build url to get question content
        val finalUrl = URLBuilder(protocol = URLProtocol.HTTPS, host = requestUrl.host,
            pathSegments = listOf("$exercisePath$exerciseId$itemPath${bodyInput.assessmentItemId}$langPath$lang")
        ).buildString()

        val harList = harContainer.requestMap[(Pair("GET", finalUrl))]

        if (harList.isNullOrEmpty()) return response

        val resultEntry = harList[0]
        val harText = resultEntry.response?.content?.text
        val data = harContainer.db.containerEntryDao.findByPathInContainer(
                harContainer.containerUid, harText ?: "") ?: return response

        GlobalScope.launch {

            val result = data.containerEntryFile?.getStringFromContainerEntry() ?: return@launch
            val itemResp = json.decodeFromString(ItemResponse.serializer(), result).itemData ?: return@launch
            var question = json.decodeFromString(ItemData.serializer(), itemResp).question?.content
                    ?: return@launch

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

            // i don't send a statement if the user is not logged in but still need to everything i did before to show user his final score
            if(harContainer.umAccount == null){
                return@launch
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
        }


        return response
    }

    private fun sendStatement(statement: String, harContainer: HarContainer) {

        GlobalScope.launch {
            httpClient.put {
                url {
                    takeFrom(harContainer.localHttp)
                    encodedPath = "${encodedPath}xapi/${harContainer.entry.contentEntryUid}/statements"
                }
                setBody(statement)
            }

        }

    }


}