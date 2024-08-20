package com.ustadmobile.core.domain.openlink

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.ext.navigateToLink

class OnClickLinkUseCase(
    private val navController: UstadNavController,
    private val accountManager: UstadAccountManager,
    private val openExternalLinkUseCase: OpenExternalLinkUseCase,
    private val userCanSelectServer: Boolean,
) {

    constructor(
        navController: UstadNavController,
        accountManager: UstadAccountManager,
        openExternalLinkUseCase: OpenExternalLinkUseCase,
        apiUrlConfig: SystemUrlConfig
    ): this(navController, accountManager, openExternalLinkUseCase, apiUrlConfig.canSelectServer)

    operator fun invoke(
        link: String,
        target: OpenExternalLinkUseCase.Companion.LinkTarget,
    ) {
        navController.navigateToLink(
            link = link,
            accountManager = accountManager,
            openExternalLinkUseCase = openExternalLinkUseCase,
            userCanSelectServer = userCanSelectServer,
            linkTarget = target,
        )
    }

}