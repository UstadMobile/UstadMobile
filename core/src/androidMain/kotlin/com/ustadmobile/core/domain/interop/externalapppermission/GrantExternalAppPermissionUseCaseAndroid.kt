package com.ustadmobile.core.domain.interop.externalapppermission

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMFileUtil


class GrantExternalAppPermissionUseCaseAndroid(
    private val storeExternalAppPermissionUseCase: StoreExternalAppPermissionUseCase,
    private val activity: Activity,
    private val db: UmAppDatabase,
    private val endpoint: Endpoint,
): GrantExternalAppPermissionUseCase {

    override suspend fun invoke(personUid: Long) {
        val permission = storeExternalAppPermissionUseCase(personUid)

        val person = db.personDao().findByUidAsync(personUid)
            ?: throw IllegalStateException("Person not in db")

        val accountName = "${person.username}@${endpoint.url}"

        val intent = Intent().apply {
            putExtra(AccountManager.KEY_ACCOUNT_NAME, accountName)
            putExtra(AccountManager.KEY_ACCOUNT_TYPE, UstadAccountManager.ACCOUNT_TYPE)
            putExtra(AccountManager.KEY_AUTHTOKEN, permission.eapAuthToken)
            putExtra("endpointUrl", endpoint.url)
            putExtra("onerosterUrl",
                UMFileUtil.joinPaths(endpoint.url, "api", "oneroster")
            )
            putExtra("sourcedId", personUid.toString())
        }

        activity.setResult(Activity.RESULT_OK, intent)
        activity.finish()
    }
}