package com.ustadmobile.port.android.presenter

import android.accounts.AccountManager
import android.content.Context
import android.util.Log
import androidx.core.os.bundleOf
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

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        eapUid = arguments[GrantAppPermissionView.ARG_PERMISSION_UID]?.toInt() ?: -1
        Log.d("ExternalAppPermission", "EAPUID = $eapUid")
    }

    fun onClickApprove() {
        presenterScope.launch {
            val db: UmAppDatabase = on(ustadAccountManager.activeEndpoint).direct.instance(
                tag = DoorTag.TAG_DB)
            val extAccessPermission = db.externalAppPermissionDao.getExternalAccessPermissionByUid(
                eapUid)
            val authToken = extAccessPermission?.eapAuthToken
            if(extAccessPermission != null && authToken != null) {
                val activeAccountName = ustadAccountManager.activeSession?.toAndroidAccount()?.name ?: "ERR"
                db.externalAppPermissionDao.grantPermission(eapUid, systemTimeInMillis(),
                    Long.MAX_VALUE)
                val activity = (context as Context).getActivityContext()
                (activity as MainActivity).setAccountAuthenticatorResult(bundleOf(
                    AccountManager.KEY_AUTHTOKEN to authToken,
                    AccountManager.KEY_ACCOUNT_TYPE to UstadAccountManagerAndroid.ACCOUNT_TYPE,
                    AccountManager.KEY_ACCOUNT_NAME to activeAccountName,
                ))
            }else {
                view.showSnackBar("ERROR")
            }
        }
    }

}