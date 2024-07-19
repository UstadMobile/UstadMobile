package com.ustadmobile.core.domain.xapi.starthttpsession

import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.lib.db.entities.UserSession

/**
 * Start an Http Xapi session
 */
interface StartXapiHttpSessionUseCase {

    suspend operator fun invoke(
        userSession: UserSession,
        accountUsername: String,
        clazzUid: Long,
        cbUid: Long = 0,
        contentEntryUid: Long = 0,
        rootActivityId: String? = null,
    ): XapiSession

}