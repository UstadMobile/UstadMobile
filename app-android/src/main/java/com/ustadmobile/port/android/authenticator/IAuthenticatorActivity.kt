package com.ustadmobile.port.android.authenticator

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle

interface IAuthenticatorActivity {

    /**
     * Set the result and finish as per docs:
     *
     *  https://developer.android.com/reference/android/accounts/AbstractAccountAuthenticator
     *  "The activity needs to return the final result when it is complete so the Intent should
     *  contain the AccountAuthenticatorResponse as"...
     */
    fun setAccountAuthenticatorResult(
        resultCode: Int,
        result: Bundle,
        resultData: Intent? = null,
    )

    /**
     * Get the ComponentName of the activity which called to request authentication.
     */
    val callingComponent: ComponentName?

}