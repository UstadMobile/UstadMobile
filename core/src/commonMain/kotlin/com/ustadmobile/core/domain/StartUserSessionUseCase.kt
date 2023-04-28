package com.ustadmobile.core.domain

import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.ext.appendQueryArgs
import com.ustadmobile.core.util.ext.navigateToViewUri
import com.ustadmobile.core.util.ext.toQueryString
import com.ustadmobile.core.view.UstadView

class StartUserSessionUseCase(
    private val accountManager: UstadAccountManager,
) {

    operator fun invoke(
        session: UserSessionWithPersonAndEndpoint,
        systemImpl: UstadMobileSystemImpl,
        context: Any,
        popUpToOnFinish: String?,
        nextDest: String,
        navController: UstadNavController,
    ) {
        accountManager.activeSession = session
        val goOptions = UstadMobileSystemCommon.UstadGoOptions(
            popUpToViewName = popUpToOnFinish,
            popUpToInclusive = false)
        val snackMsg = systemImpl.getString(MessageID.logged_in_as, context)
            .replace("%1\$s", session.person.username ?: "")
            .replace("%2\$s", session.endpoint.url)
        val dest = nextDest.appendQueryArgs(
            mapOf(UstadView.ARG_SNACK_MESSAGE to snackMsg).toQueryString())
        navController.navigateToViewUri(dest, goOptions)
    }
}
