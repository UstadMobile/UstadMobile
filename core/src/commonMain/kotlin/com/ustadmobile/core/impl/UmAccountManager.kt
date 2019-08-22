package com.ustadmobile.core.impl

import com.ustadmobile.lib.db.entities.UmAccount
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.door.asRepository
import kotlin.js.JsName
import kotlin.jvm.Synchronized
import kotlin.jvm.Volatile

object UmAccountManager {

    @Volatile
    private var activeAccount: UmAccount? = null

    private var activeAccountRepository: UmAppDatabase? = null

    private const val PREFKEY_PERSON_ID = "umaccount.personid"

    private const val PREFKEY_USERNAME = "umaccount.username"

    private const val PREFKEY_ACCESS_TOKEN = "umaccount.accesstoken"

    private const val PREFKEY_ENDPOINT_URL = "umaccount.endpointurl"

    @Synchronized
    fun getActiveAccount(context: Any, impl: UstadMobileSystemImpl): UmAccount? {
        if (activeAccount == null) {
            val personUid = impl.getAppPref(PREFKEY_PERSON_ID, "0", context).toLong()
            if (personUid == 0L)
                return null

            activeAccount = UmAccount(personUid, impl.getAppPref(PREFKEY_USERNAME, context),
                    impl.getAppPref(PREFKEY_ACCESS_TOKEN, context),
                    impl.getAppPref(PREFKEY_ENDPOINT_URL, context))
        }

        return activeAccount
    }

    @Synchronized
    fun getActivePersonUid(context: Any, impl: UstadMobileSystemImpl): Long {
        val activeAccount = getActiveAccount(context, impl)
        return activeAccount?.personUid ?: 0L
    }

    @Synchronized
    fun getActivePersonUid(context: Any): Long {
        return getActivePersonUid(context, UstadMobileSystemImpl.instance)
    }

    fun getActiveAccount(context: Any): UmAccount? {
        return getActiveAccount(context, UstadMobileSystemImpl.instance)
    }

    @Synchronized
    fun setActiveAccount(account: UmAccount?, context: Any,
                         impl: UstadMobileSystemImpl) {
        activeAccount = account
        if (account != null) {
            impl.setAppPref(PREFKEY_PERSON_ID, account.personUid.toString(), context)
            impl.setAppPref(PREFKEY_USERNAME, account.username, context)
            impl.setAppPref(PREFKEY_ACCESS_TOKEN, account.auth, context)
            impl.setAppPref(PREFKEY_ENDPOINT_URL, account.endpointUrl, context)
        } else {
            impl.setAppPref(PREFKEY_PERSON_ID, "0", context)
            impl.setAppPref(PREFKEY_USERNAME, null, context)
            impl.setAppPref(PREFKEY_ACCESS_TOKEN, null, context)
            impl.setAppPref(PREFKEY_ENDPOINT_URL, null, context)
        }
    }

    fun setActiveAccount(account: UmAccount, context: Any) {
        setActiveAccount(account, context, UstadMobileSystemImpl.instance)
    }

    @JsName("getRepositoryForActiveAccount")
    @Synchronized
    fun getRepositoryForActiveAccount(context: Any): UmAppDatabase {
        val serverUrl = UstadMobileSystemImpl.instance.getAppConfigString("apiUrl",
                "http://localhost", context) ?: "http://localhost"

        val db = UmAppDatabase.getInstance(context)
        if(activeAccountRepository == null) {
            if (activeAccount == null) {
                activeAccountRepository = db.asRepository(serverUrl, "", defaultHttpClient())!!
            }else {
                activeAccountRepository = db.asRepository(serverUrl, "", defaultHttpClient())!!
            }


        }

        return activeAccountRepository!!
    }

    fun getActiveEndpoint(context: Any): String? {
        val activeAccount = getActiveAccount(context)
        return if (activeAccount != null) {
            activeAccount.endpointUrl
        } else {
            UstadMobileSystemImpl.instance.getAppConfigString("apiUrl",
                    "http://localhost", context)
        }
    }

}
