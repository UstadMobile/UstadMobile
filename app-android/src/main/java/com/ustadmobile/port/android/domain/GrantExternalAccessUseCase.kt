package com.ustadmobile.port.android.domain

import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ExternalAppPermission
import com.ustadmobile.port.android.authenticator.IAuthenticatorActivity
import org.kodein.di.*

fun eapStringPreferencesKey(eapUid: Int) = stringPreferencesKey("${UstadAccountManager.KEY_PREFIX_EAPUID}$eapUid")

class GrantExternalAccessUseCase(
    override val di: DI
): DIAware {

    suspend operator fun invoke(
        endpoint: Endpoint,
        pendingRequestDataStore: DataStore<Preferences>,
        extAccessPermission: ExternalAppPermission,
        activeAccountName: String,
        personUid: Long,
        authenticatorActivity: IAuthenticatorActivity,
        returnAccountName: Boolean = true,
    ) {
        val db: UmAppDatabase = on(endpoint).direct.instance(tag = DoorTag.TAG_DB)

        val packageId = extAccessPermission.eapPackageId
        val authToken = extAccessPermission.eapAuthToken

        if(packageId != null && authToken != null) {
            extAccessPermission.eapPersonUid = personUid
            extAccessPermission.eapStartTime = systemTimeInMillis()
            extAccessPermission.eapExpireTime = Long.MAX_VALUE
            val prefKey = eapStringPreferencesKey(extAccessPermission.eapUid)

            pendingRequestDataStore.edit { preferences ->
                preferences.remove(prefKey)
            }

            db.externalAppPermissionDao.insertAsync(extAccessPermission)

            val resultDataIntent = if(returnAccountName) {
                Intent().apply {
                    putExtra(AccountManager.KEY_ACCOUNT_NAME, activeAccountName)
                    putExtra(AccountManager.KEY_ACCOUNT_TYPE, UstadAccountManager.ACCOUNT_TYPE)
                    putExtra(AccountManager.KEY_AUTHTOKEN, authToken)
                    putExtra("endpointUrl", endpoint.url)
                    putExtra("sourcedId", personUid.toString())
                }
            }else {
                null
            }

            authenticatorActivity.finishWithAccountAuthenticatorResult(
                Activity.RESULT_OK, resultDataIntent)
        }else {
            throw IllegalArgumentException("Null packageId or authToken: $packageId / $authToken")
        }
    }

}