package com.ustadmobile.port.android.authenticator

import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.domain.interop.externalapppermission.DeclineExternalAppPermissionUseCase
import com.ustadmobile.core.domain.interop.externalapppermission.DeclineExternalAppPermissionUseCaseAndroid
import com.ustadmobile.core.domain.interop.externalapppermission.GetExternalAppPermissionRequestInfoUseCase
import com.ustadmobile.core.domain.interop.externalapppermission.GetExternalAppPermissionRequestInfoUseCaseAndroid
import com.ustadmobile.core.domain.interop.externalapppermission.GrantExternalAppPermissionUseCase
import com.ustadmobile.core.domain.interop.externalapppermission.GrantExternalAppPermissionUseCaseAndroid
import com.ustadmobile.core.domain.interop.externalapppermission.StoreExternalAppPermissionUseCase
import com.ustadmobile.core.viewmodel.interop.externalapppermissionrequestredirect.GrantExternalAppPermissionRedirectViewModel
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.port.android.view.AbstractAppActivity
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.scoped
import org.kodein.di.singleton


/**
 * This activity handles intents that request an authentication token as per the OFFLINE_API.md
 * e.g. when an intent with action = com.ustadmobile.AUTH_GET_TOKEN is received.
 *
 * It must be a separate activity because we rely on using the activity referer to identify the
 * caller (e.g. the app to which the user is granting access permissions).
 *
 */
class AuthenticatorActivity: AbstractAppActivity() {

    override val defaultInitialRoute: String = "/${GrantExternalAppPermissionRedirectViewModel.DEST_NAME}"

    override val di by DI.lazy {
        extend(super.di)

        bind<GetExternalAppPermissionRequestInfoUseCase>() with singleton {
            GetExternalAppPermissionRequestInfoUseCaseAndroid(this@AuthenticatorActivity)
        }

        bind<StoreExternalAppPermissionUseCase>() with scoped(EndpointScope.Default).singleton {
            StoreExternalAppPermissionUseCase(
                getExternalAppPermissionRequestInfoUseCase = instance(),
                db = instance(tag = DoorTag.TAG_DB)
            )
        }

        bind<GrantExternalAppPermissionUseCase>() with scoped(EndpointScope.Default).singleton {
            GrantExternalAppPermissionUseCaseAndroid(
                storeExternalAppPermissionUseCase = instance(),
                activity = this@AuthenticatorActivity,
                db = instance(tag = DoorTag.TAG_DB),
                endpoint = context,
            )
        }

        bind<DeclineExternalAppPermissionUseCase>() with singleton {
            DeclineExternalAppPermissionUseCaseAndroid(this@AuthenticatorActivity)
        }
    }


}