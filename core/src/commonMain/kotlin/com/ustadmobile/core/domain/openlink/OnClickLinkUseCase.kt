package com.ustadmobile.core.domain.openlink

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.config.ApiUrlConfig
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.ext.navigateToLink
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class OnClickLinkUseCase(
    private val navController: UstadNavController,
    private val accountManager: UstadAccountManager,
    private val openExternalLinkUseCase: OpenExternalLinkUseCase,
    private val apiUrlConfig: ApiUrlConfig,
) {

    suspend operator fun invoke(
        link: String
    ) {
        navController.navigateToLink(
            link = link,
            accountManager = accountManager,
            openExternalLinkUseCase = openExternalLinkUseCase,
            userCanSelectServer = apiUrlConfig.canSelectServer,
        )
    }

    //Using GlobalScope is appropriate here - this is a one time quick operation that cannot be canceled
    @OptIn(DelicateCoroutinesApi::class)
    fun launchLink(
        link: String
    ) {
        GlobalScope.launch(Dispatchers.Main) { invoke(link) }
    }

}