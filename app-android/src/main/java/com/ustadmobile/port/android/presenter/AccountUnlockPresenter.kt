package com.ustadmobile.port.android.presenter

import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UserSessionWithPersonAndEndpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.core.domain.StartUserSessionUseCase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.AccountUnlockView
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class AccountUnlockPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: AccountUnlockView,
    di: DI,
): UstadBaseController<AccountUnlockView>(context, arguments, view, di) {

    private val mEndpoint = arguments[UstadView.ARG_ACCOUNT_ENDPOINT]?.let { Endpoint(it) }
        ?: throw IllegalArgumentException("No endpoint!")

    private val sessionUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong()
        ?: throw IllegalArgumentException("No entityUid for sessionUid!")

    private val accountManager: UstadAccountManager by instance()

    private val authManager: AuthManager by on(mEndpoint).instance()

    private lateinit var userSession: UserSessionWithPersonAndEndpoint

    private val startUserSessionUseCase = StartUserSessionUseCase(accountManager)

    private val systemImpl: UstadMobileSystemImpl by instance()

    private val nextDest: String? = arguments[UstadView.ARG_NEXT]

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        presenterScope.launch {
            userSession = accountManager.activeSessionsList().firstOrNull {
                it.endpoint == mEndpoint && it.userSession.usUid == sessionUid
            } ?: throw IllegalArgumentException("usersession $sessionUid not existing!")
            view.accountName = userSession.displayName
        }
    }

    fun onClickUnlock(password: String) {
        val username = userSession.person.username ?: return
        presenterScope.launch {
            try {
                val authResult = authManager.authenticate(username, password).success
                if(authResult) {
                    accountManager.setSessionLock(mEndpoint, sessionUid, false)
                    if(nextDest != null) {
                        startUserSessionUseCase(
                            session = userSession,
                            systemImpl = di.direct.instance(),
                            context = context,
                            popUpToOnFinish = arguments[UstadView.ARG_POPUPTO_ON_FINISH],
                            nextDest = nextDest,
                            navController = requireNavController(),
                        )
                    }else {
                        requireNavController().popBackStack(AccountUnlockView.VIEW_NAME, true)
                    }
                }else {
                    view.error = systemImpl.getString(MessageID.wrong_user_pass_combo, context)
                }
            }catch(e: Exception) {
                view.error = systemImpl.getString(MessageID.error, context)
            }
        }

    }

}