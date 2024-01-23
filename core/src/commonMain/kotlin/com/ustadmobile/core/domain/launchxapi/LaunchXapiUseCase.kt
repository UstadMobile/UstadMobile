package com.ustadmobile.core.domain.launchxapi

interface LaunchXapiUseCase {

    suspend operator fun invoke(contentEntryVersionUid: Long)

}