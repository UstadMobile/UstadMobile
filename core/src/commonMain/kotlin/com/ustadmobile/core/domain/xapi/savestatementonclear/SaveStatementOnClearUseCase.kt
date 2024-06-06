package com.ustadmobile.core.domain.xapi.savestatementonclear

import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.domain.xapi.model.XapiStatement

/**
 * Save xAPI statement(s) when a ViewModel is being cleared e.g. when the user navigates away
 *
 * This has to be done synchronously on the underlying platform
 *
 * On Desktop: enqueue a quartz task
 * On Android: enqueue a work request
 * On Web: If navigating within the app, just use a GlobalScope.launch that is triggered by the
 * ViewModel onClear. If navigating away, use beforeUnload to trigger SaveStatementOnUnloadUseCase
 * which will use sendBeacon.
 */
interface SaveStatementOnClearUseCase {

    operator fun invoke(
        statements: List<XapiStatement>,
        xapiSession: XapiSession,
    )

    companion object {

        const val KEY_ENDPOINT = "endpoint"

        const val KEY_STATEMENTS = "statements"

        const val KEY_XAPI_SESSION = "xapiSession"

    }

}