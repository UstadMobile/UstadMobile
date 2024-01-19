package com.ustadmobile.core.domain.account

import com.ustadmobile.core.account.AuthManager
import com.ustadmobile.core.account.UnauthorizedException

/**
 * Password set implementation that runs on the local db
 */
class SetPasswordUseCaseCommonJvm(
    private val authManager: AuthManager,
) : SetPasswordUseCase {

    override suspend operator fun invoke(
        activeUserPersonUid: Long,
        personUid: Long,
        username: String,
        newPassword: String,
        currentPassword: String?,
    ) {
        if(currentPassword != null && !authManager.authenticate(username, currentPassword).success) {
            throw UnauthorizedException("Invalid password")
        }

        authManager.setAuth(personUid, newPassword)
    }
}