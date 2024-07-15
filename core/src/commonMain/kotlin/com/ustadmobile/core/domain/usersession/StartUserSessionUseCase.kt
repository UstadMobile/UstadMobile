package com.ustadmobile.core.domain.usersession

import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.ext.appendSelectedAccount
import com.ustadmobile.core.util.ext.navigateToViewUri
import com.ustadmobile.core.viewmodel.clazz.list.ClazzListViewModel

class StartUserSessionUseCase(
    private val accountManager: UstadAccountManager,
) {

    /**
     *
     * @param dontSetCurrentSession as per UstadViewModel.ARG_DONT_SET_CURRENT_SESSION - will avoid
     *        setting the session for the UstadAccountManager.
     */
    operator fun invoke(
        session: UserSessionWithPersonAndEndpoint,
        nextDest: String = ClazzListViewModel.DEST_NAME_HOME,
        navController: UstadNavController,
        goOptions: UstadMobileSystemCommon.UstadGoOptions = UstadMobileSystemCommon.UstadGoOptions(
            clearStack = true,
        ),
        dontSetCurrentSession: Boolean = false,
    ) {
        accountManager.takeIf { !dontSetCurrentSession }?.currentUserSession = session
        navController.navigateToViewUri(
            nextDest.appendSelectedAccount(session.person.personUid, session.endpoint),
            goOptions,
        )
    }
}