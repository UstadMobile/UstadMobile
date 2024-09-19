package com.ustadmobile.core.domain.contententry.getsubtitletrackfromuri

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.contentformats.media.SubtitleTrack
import com.ustadmobile.door.DoorUri
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import js.promise.await
import kotlinx.serialization.json.Json
import web.http.fetch

class GetSubtitleTrackFromUriUseCaseJs(
    private val endpoint: Endpoint,
    private val httpClient: HttpClient,
    private val json: Json,
): GetSubtitleTrackFromUriUseCase {

    override suspend fun invoke(
        subtitleTrackUri: DoorUri,
        filename: String
    ): SubtitleTrack {
        val subtitleText = try {
            fetch(subtitleTrackUri.uri.toString()).blob().await().text().await()
        }catch(e: Throwable) {
            Napier.e("GetSubtitleTrackFromUriUseCaseJs: exception fetching local uri $subtitleTrackUri", e)
            throw IllegalStateException("Failed to fetch blob for local uri $subtitleTrackUri", e)
        }

        val subtitleJsonStr = httpClient.post(
            "${endpoint.url}api/contentupload/getsubtitletrack"
        ) {
            parameter(GetSubtitleTrackFromUriUseCase.PARAM_TRACK_FILENAME, filename)
            setBody(
                TextContent(
                    text = subtitleText,
                    contentType = ContentType.parse("text/vtt; charset=utf-8")
                )
            )
        }.bodyAsText()

        return json.decodeFromString(SubtitleTrack.serializer(), subtitleJsonStr)
    }
}