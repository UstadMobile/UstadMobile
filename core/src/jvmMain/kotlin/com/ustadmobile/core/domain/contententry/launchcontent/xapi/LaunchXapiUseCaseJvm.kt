package com.ustadmobile.core.domain.contententry.launchcontent.xapi

import com.ustadmobile.core.domain.contententry.launchcontent.LaunchContentEntryVersionUseCase
import com.ustadmobile.core.domain.getapiurl.GetApiUrlUseCase
import com.ustadmobile.core.domain.htmlcontentdisplayengine.LaunchChromeUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase

import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.lib.db.entities.ContentEntryVersion

/**
 * LaunchXapiUseCaseJvm
 *
 * Xapi Content will be launched using chrome with the --app argument (which removes the address
 * bar) and loaded from the embedded server.
 */
class LaunchXapiUseCaseJvm(
    private val resolveXapiLaunchHrefUseCase: ResolveXapiLaunchHrefUseCase,
    private val launchChromeUseCase: LaunchChromeUseCase,
    private val getApiUrlUseCase: GetApiUrlUseCase,
) : LaunchXapiUseCase {

    override suspend fun invoke(
        contentEntryVersion: ContentEntryVersion,
        navController: UstadNavController,
        clazzUid: Long,
        cbUid: Long,
        target: OpenExternalLinkUseCase.Companion.LinkTarget,
    ): LaunchContentEntryVersionUseCase.LaunchResult {
        val resolveResult = resolveXapiLaunchHrefUseCase(
            contentEntryVersionUid = contentEntryVersion.cevUid,
            clazzUid = clazzUid,
            cbUid = cbUid,
        )

        val url = getApiUrlUseCase(
            "api/content/${contentEntryVersion.cevUid}/${resolveResult.launchUriInContent}"
        )

        launchChromeUseCase(url)

        return LaunchContentEntryVersionUseCase.LaunchResult()
    }
}
