package com.ustadmobile.port.android.authenticator

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManagerAndroid
import com.ustadmobile.core.account.UstadAccountManagerAndroid.Companion.KEY_ENDPOINT
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.port.android.view.MainActivity
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import org.kodein.di.on
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.GrantAppPermissionView
import com.ustadmobile.core.view.UstadView
import kotlinx.coroutines.*

// As per https://developer.android.com/training/id-auth/custom_auth
//see https://developer.android.com/reference/kotlin/android/accounts/AbstractAccountAuthenticator
// Also helpful: http://blog.udinic.com/2013/04/24/write-your-own-android-authenticator/
class UstadAccountAuthenticator(
    private val context: Context
): AbstractAccountAuthenticator (context), DIAware{

    override val di: DI by closestDI { context }

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun editProperties(p0: AccountAuthenticatorResponse?, p1: String?): Bundle? {
        return null
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {


        val intent = Intent(context, MainActivity::class.java).apply {
            //TODO: HERE put intent info needed to start the login screen for this flow.
        }

        val reply = Bundle().apply {
            putParcelable(AccountManager.KEY_INTENT, intent)
        }

        return reply

    }

    override fun confirmCredentials(
        p0: AccountAuthenticatorResponse?,
        p1: Account?,
        p2: Bundle?
    ): Bundle? {
        return null
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse,
        account: Account,
        authTokenType: String,
        options: Bundle
    ): Bundle? {
        val endpointUrl = options.getString(KEY_ENDPOINT)
        val auth = options.getString(AccountManager.KEY_AUTHTOKEN)

        if(endpointUrl != null && auth != null) {
            val db: UmAppDatabase by di.on(Endpoint(endpointUrl)).instance(tag = DoorTag.TAG_DB)
            coroutineScope.launch {
                if(db.authTokenDao.validateToken(auth)) {
                    response.onResult(
                        bundleOf(
                            AccountManager.KEY_AUTHTOKEN to auth,
                            AccountManager.KEY_ACCOUNT_TYPE to UstadAccountManagerAndroid.ACCOUNT_TYPE,
                            AccountManager.KEY_ACCOUNT_NAME to account.name,
                        )
                    )
                }else {
                    response.onError(403, "Invalid token")
                }
            }
        }else {
            //Get application name
            // see https://stackoverflow.com/questions/11229219/android-how-to-get-application-name-not-package-name
            val authIntent = Intent(context, MainActivity::class.java).apply {
                putExtra(UstadView.ARG_OPEN_LINK, GrantAppPermissionView.VIEW_NAME)
                putExtra(UstadView.ARG_ACCOUNT_NAME, account.name)
                putExtra(UstadView.ARG_ACCOUNT_ENDPOINT,
                    AccountManager.get(context).getUserData(account, KEY_ENDPOINT))
            }

            response.onResult(Bundle().apply {
                putParcelable(AccountManager.KEY_INTENT, authIntent)
            })
        }

        return null
    }

    override fun getAuthTokenLabel(p0: String?): String {
        TODO("Not yet implemented")
    }

    override fun updateCredentials(
        p0: AccountAuthenticatorResponse?,
        p1: Account?,
        p2: String?,
        p3: Bundle?
    ): Bundle {
        TODO("Not yet implemented")
    }

    override fun hasFeatures(
        p0: AccountAuthenticatorResponse?,
        p1: Account?,
        p2: Array<out String>?
    ): Bundle {
        TODO("Not yet implemented")
    }


}