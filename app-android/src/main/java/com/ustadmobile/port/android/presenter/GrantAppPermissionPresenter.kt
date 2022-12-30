package com.ustadmobile.port.android.presenter

import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.os.bundleOf
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.account.UstadAccountManagerAndroid
import com.ustadmobile.core.controller.UstadBaseController
import com.ustadmobile.core.util.ext.toAndroidAccount
import com.ustadmobile.core.view.GrantAppPermissionView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.port.android.util.ext.getActivityContext
import com.ustadmobile.port.android.view.MainActivity
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ExternalAppPermission
import com.ustadmobile.port.android.authenticator.UstadAccountAuthenticator.Companion.KEY_PREFIX_EAPUID
import com.ustadmobile.port.android.authenticator.pendingRequestsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString

class GrantAppPermissionPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: GrantAppPermissionView,
    di: DI,
): UstadBaseController<GrantAppPermissionView>(
    context, arguments, view, di
) {

    private val ustadAccountManager: UstadAccountManager by instance()

    private var eapUid = 0

    private var returnAccountName: Boolean = false

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        eapUid = arguments[GrantAppPermissionView.ARG_PERMISSION_UID]?.toInt() ?: -1
        returnAccountName = arguments[GrantAppPermissionView.ARG_RETURN_NAME]?.toBoolean() ?: false
        Log.d("ExternalAppPermission", "EAPUID = $eapUid")
    }

    fun onClickApprove() {
        presenterScope.launch {
            val dataStore = (context as Context).pendingRequestsDataStore
            val db: UmAppDatabase = on(ustadAccountManager.activeEndpoint).direct.instance(
                tag = DoorTag.TAG_DB)
            val prefKey = stringPreferencesKey("$KEY_PREFIX_EAPUID$eapUid")

            val extAccessPermission = dataStore.data.map { preferences ->
                preferences[prefKey]?.let {
                    json.decodeFromString<ExternalAppPermission>(it)
                }
            }.first()

            val authToken = extAccessPermission?.eapAuthToken
            if(extAccessPermission != null && authToken != null) {
                val activeAccountName = ustadAccountManager.activeSession?.toAndroidAccount()?.name ?: "ERR"
                extAccessPermission.eapPersonUid = ustadAccountManager.activeSession?.person?.personUid ?: 0
                extAccessPermission.eapStartTime = systemTimeInMillis()
                extAccessPermission.eapExpireTime = Long.MAX_VALUE

                dataStore.edit { preferences ->
                    preferences.remove(prefKey)
                }

                db.externalAppPermissionDao.insertAsync(extAccessPermission)
                val activity = (context as Context).getActivityContext()

                val resultDataIntent = if(returnAccountName) {
                    Intent().apply {
                        putExtra(AccountManager.KEY_ACCOUNT_NAME, activeAccountName)
                        putExtra(AccountManager.KEY_ACCOUNT_TYPE,
                            UstadAccountManagerAndroid.ACCOUNT_TYPE)
                    }
                }else {
                    null
                }

                val resultBundle = bundleOf(
                    AccountManager.KEY_ACCOUNT_TYPE to UstadAccountManagerAndroid.ACCOUNT_TYPE,
                    AccountManager.KEY_ACCOUNT_NAME to activeAccountName,
                )

                (activity as MainActivity).setAccountAuthenticatorResult(
                    Activity.RESULT_OK, resultBundle, resultDataIntent)
            }else {
                view.showSnackBar("ERROR")
            }
        }
    }

}