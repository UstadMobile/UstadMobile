package com.ustadmobile.port.android.authenticator

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.ustadmobile.port.android.view.MainActivity

// As per https://developer.android.com/training/id-auth/custom_auth
//see https://developer.android.com/reference/kotlin/android/accounts/AbstractAccountAuthenticator
class AccountAuthenticator(
    private val context: Context
): AbstractAccountAuthenticator (context){

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
        p0: AccountAuthenticatorResponse?,
        p1: Account?,
        p2: String?,
        p3: Bundle?
    ): Bundle {
        TODO("Not yet implemented")
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