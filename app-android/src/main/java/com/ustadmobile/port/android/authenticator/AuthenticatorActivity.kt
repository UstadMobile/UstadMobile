package com.ustadmobile.port.android.authenticator

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle

import com.ustadmobile.port.android.view.UstadBaseActivity

/**
 * This activity handles intents that get created by the authenticator service. It must be a
 * separate activity because we rely on using the activity referer to identify the caller (
 * e.g. the app to which the user is granting access permissions).
 *
 */
class AuthenticatorActivity: UstadBaseActivity(), IAuthenticatorActivity {

    override var loading: Boolean = false

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

    override val callingComponent: ComponentName?
        get() = callingActivity
}