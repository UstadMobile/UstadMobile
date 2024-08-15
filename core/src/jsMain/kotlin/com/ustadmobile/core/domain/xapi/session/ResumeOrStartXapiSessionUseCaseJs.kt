package com.ustadmobile.core.domain.xapi.session

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.domain.xapi.XapiJson
import com.ustadmobile.core.domain.xapi.model.XapiActor
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.doorNodeIdHeader
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post

/**
 * Resume or start a new Xapi  session for the web client: sends the request to the server (which is
 * processed using ResumeOrStartXapiSessionRoute) so that Xapi content can then use basic auth
 */
class ResumeOrStartXapiSessionUseCaseJs(
    private val learningSpace: LearningSpace,
    private val httpClient: HttpClient,
    private val repo: UmAppDatabase,
    xapiJson: XapiJson,
): ResumeOrStartXapiSessionUseCase {

    private val json = xapiJson.json

    override suspend fun invoke(
        accountPersonUid: Long,
        actor: XapiActor,
        activityId: String,
        clazzUid: Long,
        cbUid: Long,
        contentEntryUid: Long
    ): XapiSessionEntity {
        val result: XapiSessionEntity = httpClient.post(
            "${learningSpace.url}api/xapi-ext/resumeOrStartSession"
        ) {
            doorNodeIdHeader(repo as DoorDatabaseRepository)
            parameter("accountPersonUid", accountPersonUid.toString())
            parameter("actor", json.encodeToString(XapiActor.serializer(), actor))
            parameter("activityId", activityId)
            parameter("clazzUid", clazzUid.toString())
            parameter("cbUid", cbUid.toString())
            parameter("contentEntryUid", contentEntryUid.toString())
        }.body()

        return result
    }

}