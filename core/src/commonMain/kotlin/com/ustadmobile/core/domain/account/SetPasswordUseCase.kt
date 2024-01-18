package com.ustadmobile.core.domain.account

interface SetPasswordUseCase {

    suspend operator fun invoke(
        personUid: Long,
        username: String,
        newPassword: String,
        currentPassword: String?,
    )

}