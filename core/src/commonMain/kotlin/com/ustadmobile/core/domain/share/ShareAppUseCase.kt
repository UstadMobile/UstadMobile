package com.ustadmobile.core.domain.share

interface ShareAppUseCase {
    suspend operator fun invoke(shareLink: Boolean)
}
