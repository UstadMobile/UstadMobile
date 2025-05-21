package com.ustadmobile.core.domain.username

import com.ustadmobile.core.account.LearningSpace
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText

class GetUsernameSuggestionUseCase(
    private val httpClient: HttpClient,
    private val learningSpace: LearningSpace,
)  {

     suspend operator fun invoke(
        name : String
    ) :String{
        try {
           val usernameSuggestion = httpClient.post {
                url("${learningSpace.url.removeSuffix("/")}/api/username/getsuggestion")
                parameter("name", name)
            }.bodyAsText()
            Napier.d { "GetUsernameSuggestionUseCase:  $usernameSuggestion" }
            return usernameSuggestion
        } catch (e: Throwable) {
            Napier.d { "GetUsernameSuggestionUseCase:-  exception $e" }
            throw e
        }
    }
}