package com.ustadmobile.core.domain.account

interface SetPasswordUseCase {

    /**
     * @param activeUserPersonUid the personUid for the currently active user (e.g. the user with
     *        an active session)
     * @param personUid the personUid for which we are changing the password
     * @param username the username for which we are changing the password
     * @param newPassword the new password to set
     * @param currentPassword if the user does not have password reset permission, then they must
     *        provide their current valid password.
     */
    suspend operator fun invoke(
        activeUserPersonUid: Long,
        personUid: Long,
        username: String,
        newPassword: String,
        currentPassword: String?,
    )

}