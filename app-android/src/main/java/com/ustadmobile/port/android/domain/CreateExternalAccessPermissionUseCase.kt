package com.ustadmobile.port.android.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.lib.db.entities.ExternalAppPermission
import com.ustadmobile.port.android.authenticator.IAuthenticatorActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.DIAware
import java.util.*

class CreateExternalAccessPermissionUseCase(
    private val json: Json,
    override val di: DI,
): DIAware {

    suspend operator fun invoke(
        eapUidArg: Int = 0,
        pendingRequestDataStore: DataStore<Preferences>,
        authenticatorActivity: IAuthenticatorActivity
    ) : ExternalAppPermission{
        val prefKey = eapStringPreferencesKey(eapUidArg)
        return pendingRequestDataStore.data.map { preferences ->
            preferences[prefKey]?.let {
                json.decodeFromString<ExternalAppPermission>(it)
            }
        }.first() ?: ExternalAppPermission(
            eapPackageId = authenticatorActivity.callingComponent?.packageName,
            eapAuthToken = UUID.randomUUID().toString(),
        )
    }

}
