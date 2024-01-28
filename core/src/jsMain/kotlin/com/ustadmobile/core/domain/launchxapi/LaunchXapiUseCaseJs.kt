package com.ustadmobile.core.domain.launchxapi

import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.xapicontent.XapiContentViewModel

class LaunchXapiUseCaseJs: LaunchXapiUseCase {

    override suspend fun invoke(
        contentEntryVersionUid: Long,
        navController: UstadNavController
    ) : LaunchXapiUseCase.LaunchResult{
        navController.navigate(
            viewName = XapiContentViewModel.DEST_NAME,
            args = mapOf(UstadViewModel.ARG_ENTITY_UID to contentEntryVersionUid.toString())
        )
        return LaunchXapiUseCase.LaunchResult(null)
    }

}