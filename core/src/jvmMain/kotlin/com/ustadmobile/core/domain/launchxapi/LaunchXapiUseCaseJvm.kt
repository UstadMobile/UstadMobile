package com.ustadmobile.core.domain.launchxapi

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.htmlcontentdisplayengine.GetChromePathUseCase
import com.ustadmobile.core.embeddedhttp.EmbeddedHttpServer
import com.ustadmobile.core.impl.nav.UstadNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

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
    private val dataDir: File,
    private val getChromePathUseCase: GetChromePathUseCase,
) : LaunchXapiUseCase{

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    override suspend fun invoke(
        contentEntryVersionUid: Long,
        navController: UstadNavController
    ): LaunchXapiUseCase.LaunchResult {
        val resolveResult = resolveXapiLaunchHrefUseCase(
            contentEntryVersionUid,
        )

        val chromePath = getChromePathUseCase() ?: throw IllegalStateException("Can't find Chrome")

        val url = embeddedHttpServer.endpointUrl(
            endpoint = endpoint,
            path = "api/content/${contentEntryVersionUid}/${resolveResult.launchUriInContent}"
        )

        scope.launch {
            ProcessBuilder(chromePath, "--app=$url")
                .directory(dataDir)
                .start()
                .waitFor()
        }

        return LaunchXapiUseCase.LaunchResult(chromePath)
    }
}