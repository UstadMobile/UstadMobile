package com.ustadmobile.core.domain.interop.externalapppermission

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import com.ustadmobile.core.account.UstadAccountManager


class GrantExternalAppPermissionUseCaseAndroid(
    private val storeExternalAppPermissionUseCase: StoreExternalAppPermissionUseCase,
    private val activity: Activity,
    private val accountManager: UstadAccountManager,
): GrantExternalAppPermissionUseCase {

    override suspend fun invoke(personUid: Long) {
        val permission = storeExternalAppPermissionUseCase(personUid)

        val accountName = "${accountManager.currentAccount.username}@${accountManager.activeEndpoint.url}"

        val intent = Intent().apply {
            putExtra(AccountManager.KEY_ACCOUNT_NAME, accountName)
            putExtra(AccountManager.KEY_ACCOUNT_TYPE, UstadAccountManager.ACCOUNT_TYPE)
            putExtra(AccountManager.KEY_AUTHTOKEN, permission.eapAuthToken)
            putExtra("endpointUrl", accountManager.activeEndpoint.url)
            putExtra("sourcedId", personUid.toString())
        }

        activity.setResult(Activity.RESULT_OK, intent)
        activity.finish()
    }
}