package com.ustadmobile.core.domain.contententry.launchcontent.xapi

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.contententry.launchcontent.LaunchContentEntryVersionUseCase
import com.ustadmobile.core.domain.htmlcontentdisplayengine.LaunchChromeUseCase
import com.ustadmobile.core.embeddedhttp.EmbeddedHttpServer
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.lib.db.entities.ContentEntryVersion

/**
 * LaunchXapiUseCaseJvm
 *
 * Xapi Content will be launched using chrome with the --app argument (which removes the address
 * bar) and loaded from the embedded server.
 */
class LaunchXapiUseCaseJvm(
    private val endpoint: Endpoint,
    private val resolveXapiLaunchHrefUseCase: ResolveXapiLaunchHrefUseCase,
    private val embeddedHttpServer: EmbeddedHttpServer,
    private val launchChromeUseCase: LaunchChromeUseCase,
) : LaunchXapiUseCase {

    override suspend fun invoke(
        contentEntryVersion: ContentEntryVersion,
        navController: UstadNavController
    ): LaunchContentEntryVersionUseCase.LaunchResult {
        val resolveResult = resolveXapiLaunchHrefUseCase(
            contentEntryVersion.cevUid,
        )

        val url = embeddedHttpServer.endpointUrl(
            endpoint = endpoint,
            path = "api/content/${contentEntryVersion.cevUid}/${resolveResult.launchUriInContent}"
        )

        launchChromeUseCase(url)

        return LaunchContentEntryVersionUseCase.LaunchResult()
    }
}
