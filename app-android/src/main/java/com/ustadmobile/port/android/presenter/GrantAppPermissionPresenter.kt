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
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ExternalAppPermission
import com.ustadmobile.port.android.authenticator.IAuthenticatorActivity
import com.ustadmobile.port.android.authenticator.UstadAccountAuthenticator.Companion.KEY_PREFIX_EAPUID
import com.ustadmobile.port.android.authenticator.pendingRequestsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import java.util.*

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

    private var mExtAccessPermission: ExternalAppPermission? = null

    val prefKey = stringPreferencesKey("$KEY_PREFIX_EAPUID$eapUid")

    private lateinit var authenticatorActivity: IAuthenticatorActivity


    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        eapUid = arguments[GrantAppPermissionView.ARG_PERMISSION_UID]?.toInt() ?: -1
        returnAccountName = arguments[GrantAppPermissionView.ARG_RETURN_NAME]?.toBoolean() ?: false

        val dataStore = (context as Context).pendingRequestsDataStore
        val activity = (context as Context).getActivityContext()
        authenticatorActivity = (activity as IAuthenticatorActivity)

        presenterScope.launch {
            mExtAccessPermission = dataStore.data.map { preferences ->
                preferences[prefKey]?.let {
                    json.decodeFromString<ExternalAppPermission>(it)
                }
            }.first() ?: ExternalAppPermission(
                eapPackageId = authenticatorActivity.callingComponent?.packageName,
                eapAuthToken = UUID.randomUUID().toString(),
            )
        }

        val packageManager = (context as Context).packageManager
        val callingComp = authenticatorActivity.callingComponent
        if(callingComp != null) {
            val activityInfo = packageManager.getActivityInfo(callingComp, 0)
            view.grantToAppName = activityInfo.loadLabel(packageManager).toString()
            view.grantToIcon = activityInfo.loadIcon(packageManager)
        }
    }

    fun onClickApprove() {
        val extAccessPermission = mExtAccessPermission ?: return
        presenterScope.launch {
            val db: UmAppDatabase = on(ustadAccountManager.activeEndpoint).direct.instance(
                tag = DoorTag.TAG_DB)

            val packageId = extAccessPermission.eapPackageId

            val authToken = extAccessPermission.eapAuthToken
            if(packageId != null && authToken != null) {
                val activeAccountName = ustadAccountManager.activeSession?.toAndroidAccount()?.name ?: "ERR"
                extAccessPermission.eapPersonUid = ustadAccountManager.activeSession?.person?.personUid ?: 0
                extAccessPermission.eapStartTime = systemTimeInMillis()
                extAccessPermission.eapExpireTime = Long.MAX_VALUE

                val dataStore = (context as Context).pendingRequestsDataStore

                dataStore.edit { preferences ->
                    preferences.remove(prefKey)
                }

                db.externalAppPermissionDao.insertAsync(extAccessPermission)

                val resultDataIntent = if(returnAccountName) {
                    Intent().apply {
                        putExtra(AccountManager.KEY_ACCOUNT_NAME, activeAccountName)
                        putExtra(AccountManager.KEY_ACCOUNT_TYPE,
                            UstadAccountManagerAndroid.ACCOUNT_TYPE)
                        putExtra(AccountManager.KEY_AUTHTOKEN, authToken)
                    }
                }else {
                    null
                }

                val resultBundle = bundleOf(
                    AccountManager.KEY_ACCOUNT_TYPE to UstadAccountManagerAndroid.ACCOUNT_TYPE,
                    AccountManager.KEY_ACCOUNT_NAME to activeAccountName,
                )

                authenticatorActivity.setAccountAuthenticatorResult(
                    Activity.RESULT_OK, resultBundle, resultDataIntent)
            }else {
                view.showSnackBar("ERROR")
            }
        }
    }

}