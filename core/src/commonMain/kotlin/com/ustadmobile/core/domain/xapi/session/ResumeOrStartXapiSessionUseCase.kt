package com.ustadmobile.core.domain.xapi.session

import com.ustadmobile.core.domain.xapi.model.XapiActor
import com.ustadmobile.lib.db.entities.xapi.XapiSessionEntity

/**
 * Resume an existing xAPI session (if the most recent session, if any exists for the given actor
 * and activity params is not marked as complete) or create a new session otherwise.
 *
 * On Android and Desktop (JVM) this works by directly creating a new XapiSession in the database
 * if there is no incomplete pending session.
 *
 * On Web (JS) this works by contacting the server, which then creates or returns a XapiSession
 */
interface ResumeOrStartXapiSessionUseCase {

    suspend operator fun invoke(
        accountPersonUid: Long,
        actor: XapiActor,
        activityId: String,
        clazzUid: Long,
        cbUid: Long,
        contentEntryUid: Long,
    ) : XapiSessionEntity

}