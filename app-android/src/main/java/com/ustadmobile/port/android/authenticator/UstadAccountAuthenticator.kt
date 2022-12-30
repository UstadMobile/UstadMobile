package com.ustadmobile.port.android.authenticator

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManagerAndroid
import com.ustadmobile.core.account.UstadAccountManagerAndroid.Companion.USERDATA_KEY_ENDPOINT
import com.ustadmobile.core.account.UstadAccountManagerAndroid.Companion.USERDATA_KEY_PERSONUID
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.port.android.view.MainActivity
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import org.kodein.di.on
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UstadUrlComponents
import com.ustadmobile.core.view.GrantAppPermissionView
import com.ustadmobile.core.view.GrantAppPermissionView.Companion.ARG_PERMISSION_UID
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ExternalAppPermission
import kotlinx.coroutines.*
import java.util.UUID
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

/**
 * Storage of pending requests for approval. This is where we have issued an intent for the user to
 * approve or deny, but we don't (yet) have approval. In the case of using add account, we don't
 * know what endpoint is being used, so we can't put in the database.
 */
internal val Context.pendingRequestsDataStore:
    DataStore<Preferences> by preferencesDataStore(name = "authenticator_pending_requests")

// As per https://developer.android.com/training/id-auth/custom_auth
// see https://developer.android.com/reference/kotlin/android/accounts/AbstractAccountAuthenticator
// Useful source: https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/accounts/AccountAuthenticatorActivity.java
// Also helpful: http://blog.udinic.com/2013/04/24/write-your-own-android-authenticator/
class UstadAccountAuthenticator(
    private val context: Context
): AbstractAccountAuthenticator (context), DIAware{

    override val di: DI by closestDI { context }

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private val json: Json by di.instance()

    override fun editProperties(p0: AccountAuthenticatorResponse?, p1: String?): Bundle? {
        return null
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle
    ): Bundle {
        val callerUid = options.getInt(AccountManager.KEY_CALLER_UID)

        return runBlocking {
            val eapUid = Random.nextInt(0, Int.MAX_VALUE)

            val externalAppPermission = ExternalAppPermission(
                eapCallerUid = callerUid,
                eapAuthToken = UUID.randomUUID().toString(),
            )

            context.pendingRequestsDataStore.edit {  prefs ->
                prefs[stringPreferencesKey("$KEY_PREFIX_EAPUID$eapUid")] = json.encodeToString(
                    externalAppPermission)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra(UstadView.ARG_OPEN_LINK,
                    "${GrantAppPermissionView.VIEW_NAME}?$ARG_PERMISSION_UID=$eapUid" +
                        "&${GrantAppPermissionView.ARG_RETURN_NAME}=true")
            }
            Bundle().apply {
                putParcelable(AccountManager.KEY_INTENT, intent)
            }
        }
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
        val endpointUrl = AccountManager.get(context).getUserData(account, USERDATA_KEY_ENDPOINT)
        val personUid = AccountManager.get(context).getUserData(account, USERDATA_KEY_PERSONUID).toLong()

        //check the caller - if permissions have already been granted for this caller, then return
        // a token. Otherwise, return an intent tha tit will have to launch
        val callerUid = options.getInt(AccountManager.KEY_CALLER_UID)
        val db: UmAppDatabase by di.on(Endpoint(endpointUrl)).instance(tag = DoorTag.TAG_DB)

        coroutineScope.launch {
            val grantedToken = db.externalAppPermissionDao.getGrantedAuthToken(
                callerUid, personUid, systemTimeInMillis())
            if(grantedToken != null) {
                response.onResult(
                    bundleOf(
                        AccountManager.KEY_AUTHTOKEN to grantedToken,
                        AccountManager.KEY_ACCOUNT_TYPE to UstadAccountManagerAndroid.ACCOUNT_TYPE,
                        AccountManager.KEY_ACCOUNT_NAME to account.name,
                    )
                )
            }else {
                //Get application name
                // see https://stackoverflow.com/questions/11229219/android-how-to-get-application-name-not-package-name
                val eapUid = Random.nextInt(0, Int.MAX_VALUE)
                val externalAppPermission = ExternalAppPermission(
                    eapPersonUid = personUid,
                    eapCallerUid = callerUid,
                    eapAuthToken = UUID.randomUUID().toString(),
                    eapAndroidAccountName = account.name,
                    eapUid = eapUid
                )
                context.pendingRequestsDataStore.edit {
                    it[stringPreferencesKey("eap_$eapUid")] = json.encodeToString(
                        externalAppPermission)
                }

                val authIntent = Intent(context, MainActivity::class.java).apply {
                    putExtra(
                        UstadView.ARG_OPEN_LINK, UstadUrlComponents(endpointUrl,
                        GrantAppPermissionView.VIEW_NAME,
                            "$ARG_PERMISSION_UID=${externalAppPermission.eapUid}").fullUrl())
                    putExtra(UstadView.ARG_ACCOUNT_NAME, account.name)
                    putExtra(UstadView.ARG_ACCOUNT_ENDPOINT, endpointUrl)
                }

                response.onResult(Bundle().apply {
                    putParcelable(AccountManager.KEY_INTENT, authIntent)
                })
            }
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


    companion object {

        const val KEY_PREFIX_EAPUID = "eap_"

    }
}