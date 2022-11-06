package com.ustadmobile.core.account

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Build
import androidx.core.os.bundleOf
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.Person
import org.kodein.di.DI

class UstadAccountManagerAndroid(
    systemImpl: UstadMobileSystemImpl,
    appContext: Context,
    di: DI
) : UstadAccountManager(systemImpl, appContext, di){

    private val androidContext = appContext

    fun UserSessionWithPersonAndEndpoint.toAndroidAccount() : Account{
        val displayUrl = endpoint.url
            .removePrefix("http://")
            .removePrefix("https://")
            .removeSuffix("/")
        return Account("${this.person.username}@$displayUrl", ACCOUNT_TYPE)
    }

    override suspend fun addSession(
        person: Person,
        endpointUrl: String,
        password: String?
    ): UserSessionWithPersonAndEndpoint {
        val sessionCreated = super.addSession(person, endpointUrl, password)
        val androidAccountManager = AccountManager.get(androidContext)
        val accountedAdded = androidAccountManager.addAccountExplicitly(sessionCreated.toAndroidAccount(),
            sessionCreated.userSession.usAuth ?: throw IllegalArgumentException("No usersession auth!"),
            bundleOf(KEY_ENDPOINT to endpointUrl))
        if(!accountedAdded) {
            throw IllegalStateException("Could not add account to AccountManager!")
        }

        return sessionCreated
    }

    override suspend fun endSession(
        session: UserSessionWithPersonAndEndpoint,
        endStatus: Int,
        endReason: Int
    ) {
        super.endSession(session, endStatus, endReason)
        val androidAccountManager = AccountManager.get(androidContext)

        //TODO: Handle SDK 21
        if(Build.VERSION.SDK_INT >= 22)
            androidAccountManager.removeAccountExplicitly(session.toAndroidAccount())
    }

    companion object {

        const val ACCOUNT_TYPE = "com.ustadmobile"

        const val INTENT_KEY_GET_AUTH_TOKEN = "getAuthToken"

        const val OPTIONS_KEY_AUTH = "auth"

        const val KEY_ENDPOINT = "endpointUrl"

        //getaccount return keys etc. here
        const val KEY_OPT = "opt"

    }
}