package com.ustadmobile.core.domain.respect

import android.content.Context
import android.content.Intent
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.lib.db.entities.respect.RespectApp
import com.ustadmobile.lib.db.entities.respect.RespectAssignment
import com.ustadmobile.lib.db.entities.respect.RespectLesson
import androidx.core.net.toUri
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.getapiurl.GetApiUrlUseCase
import com.ustadmobile.core.domain.xapi.ext.agent
import com.ustadmobile.core.domain.xapi.ext.authorizationHeader
import com.ustadmobile.core.domain.xapi.ext.registrationUuid
import com.ustadmobile.core.domain.xapi.model.XapiAgent
import com.ustadmobile.core.domain.xapi.session.ResumeOrStartXapiSessionUseCase
import com.ustadmobile.core.util.ext.appendQueryArgs
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json

class RespectLaunchUseCaseAndroid(
    private val context: Context,
    private val accountManager: UstadAccountManager,
    private val resumeOrStartXapiSessionUseCase: ResumeOrStartXapiSessionUseCase,
    private val getApiUrlUseCase: GetApiUrlUseCase,
    private val json: Json,
    private val endpoint: Endpoint,
): RespectLaunchUseCase {

    override suspend fun invoke(
        app: RespectApp,
        lesson: RespectLesson?,
        assignment: RespectAssignment?
    ) {
        val openUri = lesson?.rlUri
        val intent = if(openUri != null) {
            val xapiSession = resumeOrStartXapiSessionUseCase(
                accountPersonUid = accountManager.currentUserSession.person.personUid,
                actor = accountManager.currentUserSession.toXapiAgent(),
                activityId = openUri,
                clazzUid = 0,
                cbUid = 0,
                contentEntryUid = lesson.rlUid,
                contentEntryVersionUid = 0,
            )

            val queryParams: Map<String, String> = mapOf(
                "endpoint" to getApiUrlUseCase("/api/xapi/"),
                "auth" to xapiSession.authorizationHeader(),
                "actor" to json.encodeToString(XapiAgent.serializer(), xapiSession.agent(endpoint)),
                "registration" to xapiSession.registrationUuid.toString(),
                "activity_id" to xapiSession.xseRootActivityId,
                "respectLaunchVersion" to "1",
                "given_name" to (accountManager.currentUserSession.person.firstNames ?: "")
            )

            val launchUri = openUri.appendQueryArgs(queryParams).toUri()

            Napier.i("RespectLaunchUseCase: Launch $launchUri")
            Intent(Intent.ACTION_VIEW, launchUri)
        }else {
            context.packageManager.getLaunchIntentForPackage(app.raAndroidPackageName)
        }

        if(intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}