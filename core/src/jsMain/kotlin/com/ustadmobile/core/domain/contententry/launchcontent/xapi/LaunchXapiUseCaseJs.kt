package com.ustadmobile.core.domain.contententry.launchcontent.xapi

import com.ustadmobile.core.domain.contententry.launchcontent.LaunchContentEntryVersionUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.ext.asWindowTarget
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import io.github.aakira.napier.Napier
import web.location.location
import web.window.window

class LaunchXapiUseCaseJs(
    private val resolveXapiLaunchHrefUseCase: ResolveXapiLaunchHrefUseCase,
) : LaunchXapiUseCase{

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

        if(target != OpenExternalLinkUseCase.Companion.LinkTarget.TOP) {
            Napier.d { "LaunchXapiUseCaseJs: launching xapi in new windows: ${resolveResult.url}" }
            window.open(resolveResult.url, target.asWindowTarget(), features = "popup=true,noopener,noreferrer")
        }else {
            Napier.d { "LaunchXapiUseCaseJs: navigating to: ${resolveResult.url}" }
            location.href = resolveResult.url
        }

        return LaunchContentEntryVersionUseCase.LaunchResult()
    }
}