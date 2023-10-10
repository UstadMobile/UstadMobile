package com.ustadmobile.mui.components

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.components.DIContext
import com.ustadmobile.core.hooks.collectAsState
import com.ustadmobile.core.util.ext.initials
import com.ustadmobile.core.viewmodel.accountlist.AccountListViewModel
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UserSession
import mui.material.Avatar
import mui.material.IconButton
import org.kodein.di.direct
import org.kodein.di.instance
import react.FC
import react.Props
import react.router.useNavigate
import react.useRequiredContext

val HeaderAvatar = FC<Props> {
    val appDi = useRequiredContext(DIContext)
    val navigateFn = useNavigate()
    val accountManager: UstadAccountManager = appDi.di.direct.instance()
    val currentSession: UserSessionWithPersonAndEndpoint by accountManager.currentUserSessionFlow
        .collectAsState(
            UserSessionWithPersonAndEndpoint(
                userSession = UserSession(),
                person = Person(),
                endpoint = Endpoint("")
            )
        )

    IconButton {
        onClick = {
            navigateFn.invoke(AccountListViewModel.DEST_NAME)
        }
        Avatar {
            + currentSession.person.initials()
        }
    }
}