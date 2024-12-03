package com.ustadmobile.core.domain.matomo

interface RecordMatomoTrackingUseCase {
    suspend fun invoke(path: String, title: String)
}