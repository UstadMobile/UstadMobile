package com.ustadmobile.core.domain.account

import com.ustadmobile.core.db.UmAppDataLayer
import io.github.aakira.napier.Napier

/**
 * Check if registration is allowed on a particular learning space. This is sometimes needed during
 * navigation flows; if new user registration is allowed we will use AddAccountSelectNewOrExisting,
 * if registration is not possible, then we go can go directly to Login
 */
class CheckRegistrationAllowedUseCase(
    private val dataLayer: UmAppDataLayer,
) {

    /**
     * @param destUri the destination uri the user is navigating to. This can affect whether or not
     * registration is allowed e.g. the destination uri might contain an invite token. Reserved for
     * future use.
     * @return true if registration is definitely allowed, false when definitely not allowed, null
     * if not known.
     */
    suspend operator fun invoke(
        @Suppress("UNUSED_PARAMETER") destUri: String
    ): Boolean? {
        val localDbSiteEntity = dataLayer.localDb.siteDao().getSiteAsync()
        return try {
            dataLayer.repository?.siteDao()?.getSiteAsync()?.registrationAllowed
        }catch (e: Exception) {
            Napier.d("GetRegistrationAllowedUseCase: Error occurred: ${e.message}", throwable = e)
            localDbSiteEntity?.registrationAllowed
        }
    }

}