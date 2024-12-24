package com.ustadmobile.core.domain.password

interface LoginWithSavedPasswordUseCase {
    suspend operator fun invoke():LoginWithSavePasswordResult
}
