package com.ustadmobile.core.domain.contententry.launchcontent.xapi

import android.content.Context

import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsSession
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.domain.contententry.launchcontent.LaunchContentEntryVersionUseCase
import com.ustadmobile.core.domain.getapiurl.GetApiUrlUseCase
import com.ustadmobile.core.domain.htmlcontentdisplayengine.GetHtmlContentDisplayEngineUseCase
import com.ustadmobile.core.domain.htmlcontentdisplayengine.HTML_ENGINE_USE_CHROMETAB
import com.ustadmobile.core.domain.htmlcontentdisplayengine.HTML_ENGINE_USE_WEBVIEW
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.core.domain.xapi.XapiSession
import com.ustadmobile.core.embeddedhttp.EmbeddedHttpServer
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.core.viewmodel.xapicontent.XapiContentViewModel
import com.ustadmobile.lib.db.entities.ContentEntryVersion


class LaunchXapiUseCaseAndroid(
    private val androidContext: Context,
    private val endpoint: Endpoint,
    private val getHtmlContentDisplayEngineUseCase: GetHtmlContentDisplayEngineUseCase,
    private val resolveXapiLaunchHrefUseCase: ResolveXapiLaunchHrefUseCase,
    private val lightToolbarColor: Int, //Can use jetpack compose color .toArgb()
    private val darkToolbarColor: Int,
    private val session: () -> CustomTabsSession?,
    private val embeddedHttpServer: EmbeddedHttpServer,
    private val getApiUrlUseCase: GetApiUrlUseCase,
): LaunchXapiUseCase {

    override suspend fun invoke(
        contentEntryVersion: ContentEntryVersion,
        navController: UstadNavController,
        target: OpenExternalLinkUseCase.Companion.LinkTarget,
        xapiSession: XapiSession?,
    ): LaunchContentEntryVersionUseCase.LaunchResult {
        val htmlContentEngine = getHtmlContentDisplayEngineUseCase()
        when(htmlContentEngine.code) {
            HTML_ENGINE_USE_CHROMETAB -> {
                val resolveResult = resolveXapiLaunchHrefUseCase(
                    contentEntryVersion.cevUid,
                )

                //Should be replaced with using GetApiUrlUseCase
                val url = getApiUrlUseCase(
                    "/api/content/${contentEntryVersion.cevUid}/${resolveResult.launchUriInContent}"
                )

                /**
                 * The Custom Tab Session is a nice-to-have that will almost always be available, so we can
                 * use warm up etc. The docs mention that the service might disconnect for short periods.
                 */
                val customTabSession = session()

                val customTabIntent = CustomTabsIntent.Builder()
                    .setUrlBarHidingEnabled(false)
                    .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                    .setBookmarksButtonEnabled(false)
                    .setDownloadButtonEnabled(false)
                    .let {
                        if(customTabSession != null)
                            it.setSession(customTabSession)
                        else
                            it
                    }
                    .setDefaultColorSchemeParams(
                        CustomTabColorSchemeParams.Builder()
                            .setToolbarColor(lightToolbarColor)
                            .build()
                    )
                    .setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_DARK,
                        CustomTabColorSchemeParams.Builder()
                            .setToolbarColor(darkToolbarColor)
                            .build()
                    )
                    .setStartAnimations(androidContext,
                        moe.tlaster.precompose.viewmodel.R.anim.abc_fade_in,
                        moe.tlaster.precompose.viewmodel.R.anim.abc_fade_out
                    )
                    .setShowTitle(true)
                    .build()

                customTabIntent.launchUrl(androidContext, Uri.parse(url))
            }

            HTML_ENGINE_USE_WEBVIEW -> {
                navController.navigate(
                    viewName = XapiContentViewModel.DEST_NAME,
                    args = mapOf(UstadViewModel.ARG_ENTITY_UID to contentEntryVersion.cevUid.toString())
                )
            }
        }

        return LaunchContentEntryVersionUseCase.LaunchResult(null)

    }
}