package com.ustadmobile.libuicompose.util

import androidx.compose.ui.platform.UriHandler
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.core.impl.config.SystemUrlConfig
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.ext.navigateToLink

/**
 * Implements a Compose UriHandler via the UstadNavController.navigateToLink function
 */
class NavControllerUriHandler(
    private val navController: UstadNavController,
    private val accountManager: UstadAccountManager,
    private val openExternalLinkUseCase: OpenExternalLinkUseCase,
    private val apiUrlConfig: SystemUrlConfig,
) : UriHandler{

    override fun openUri(uri: String) {
        navController.navigateToLink(
            link = uri,
            accountManager = accountManager,
            openExternalLinkUseCase = openExternalLinkUseCase,
            userCanSelectServer = apiUrlConfig.canSelectServer,
        )
    }
}