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

    /**
     * @param contentEntryVersionUid Content created by authoring tools (including Articulate) can
     * crash when data from a previous version of the content is loaded by a new version (where the
     * new version of the content has the same activity id as the old version). This is poor error
     * handling on the part of the authoring tool output, but there's not much we can do about that.
     * Hence we only consider sessions to resume where that session was created for the same
     * contentEntryVersionUid. See UstadMobile Github Issue #970 for further info.
     */
    suspend operator fun invoke(
        accountPersonUid: Long,
        actor: XapiActor,
        activityId: String,
        clazzUid: Long,
        cbUid: Long,
        contentEntryUid: Long,
        contentEntryVersionUid: Long,
    ) : XapiSessionEntity

}