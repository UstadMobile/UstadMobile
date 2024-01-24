package com.ustadmobile.core.domain.launchxapi

import com.ustadmobile.core.impl.nav.UstadNavController

interface LaunchXapiUseCase {

    suspend operator fun invoke(
        contentEntryVersionUid: Long,
        navController: UstadNavController,
    )

}