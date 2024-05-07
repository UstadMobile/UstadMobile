package com.ustadmobile.port.android.authenticator

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import com.ustadmobile.core.domain.interop.externalapppermission.GetExternalAppPermissionRequestInfoUseCase
import com.ustadmobile.core.domain.interop.externalapppermission.GetExternalAppPermissionRequestInfoUseCaseAndroid
import com.ustadmobile.core.viewmodel.interop.externalapppermissionrequestredirect.GrantExternalAppPermissionRedirectViewModel
import com.ustadmobile.port.android.view.AbstractAppActivity
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton


/**
 * This activity handles intents that get created by the authenticator service. It must be a
 * separate activity because we rely on using the activity referer to identify the caller (
 * e.g. the app to which the user is granting access permissions).
 *
 */
class AuthenticatorActivity: AbstractAppActivity(), IAuthenticatorActivity {

    override val defaultInitialRoute: String = "/${GrantExternalAppPermissionRedirectViewModel.DEST_NAME}"

    override val callingComponent: ComponentName?
        get() = callingActivity

    override val di by DI.lazy {
        extend(super.di)

        bind<GetExternalAppPermissionRequestInfoUseCase>() with singleton {
            GetExternalAppPermissionRequestInfoUseCaseAndroid(this@AuthenticatorActivity)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun finishWithAccountAuthenticatorResult(
        resultCode: Int,
        resultData: Intent?,
    ) {
        if(resultData == null) {
            setResult(resultCode)
        }else {
            setResult(resultCode, resultData)
        }

        finish()
    }

}