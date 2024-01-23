package com.ustadmobile.core.domain.launchxapi

import android.content.Context

import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsSession


/**
 * Launching Xapi must be done using a Custom Chrome Tab on Android because:
 *
 * 1) Using a custom resource loader for WebView doesn't work with video that may preload:
 *    https://github.com/ionic-team/capacitor/issues/6021
 *
 * 2) WebViews don't provide all the features provided by the browser as per
 *    https://developer.chrome.com/docs/android/custom-tabs
 *
 *    Xapi exports are tested by vendors to work in browsers, not WebView. This can make the results
 *    unreliable when using different style units, etc.
 *
 *  Xapi can still run offline because we will use an embedded local http server.
 */
class LaunchXapiUseCaseAndroid(
    private val androidContext: Context,
    private val resolveXapiLaunchHrefUseCase: ResolveXapiLaunchHrefUseCase,
    private val lightToolbarColor: Int, //Can use jetpack compose color .toArgb()
    private val darkToolbarColor: Int,
    private val session: () -> CustomTabsSession?,
): LaunchXapiUseCase {

    override suspend fun invoke(contentEntryVersionUid: Long) {
        val resolveResult = resolveXapiLaunchHrefUseCase(
            contentEntryVersionUid,
        )

        /**
         * https://chromium.googlesource.com/external/github.com/GoogleChrome/custom-tabs-client/+/380a1c31040671699f8ccb81830b5c75c80327ec/README.md#lifecycle
         */
        val customTabIntent = CustomTabsIntent.Builder()
            .setUrlBarHidingEnabled(false)
            .setBookmarksButtonEnabled(false)
            .setDownloadButtonEnabled(false)
            .setSession(session()!!)
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

        customTabIntent.launchUrl(androidContext, Uri.parse(resolveResult.url))

    }
}