package com.ustadmobile.core.domain.launchxapi

import com.ustadmobile.core.impl.nav.UstadNavController

interface LaunchXapiUseCase {

    data class LaunchResult(
        val message: String? = null
    )

    suspend operator fun invoke(
        contentEntryVersionUid: Long,
        navController: UstadNavController,
    ): LaunchResult?

}