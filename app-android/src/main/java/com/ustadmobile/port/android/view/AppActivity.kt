package com.ustadmobile.port.android.view

import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.compose.ui.graphics.toArgb
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.jakewharton.processphoenix.ProcessPhoenix
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.domain.contententry.launchcontent.xapi.LaunchXapiUseCase
import com.ustadmobile.core.domain.contententry.launchcontent.xapi.LaunchXapiUseCaseAndroid
import com.ustadmobile.libuicompose.theme.md_theme_dark_primaryContainer
import com.ustadmobile.libuicompose.theme.md_theme_light_primaryContainer
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider
import org.kodein.di.scoped


class AppActivity: AbstractAppActivity() {

    //As per https://developer.chrome.com/docs/android/custom-tabs/guide-warmup-prefetch/
    private var mCustomTabsClient: CustomTabsClient? = null

    private var mCustomTabsSession: CustomTabsSession? = null

    override val di by DI.lazy {
        extend(super.di)

        bind<LaunchXapiUseCase>() with scoped(EndpointScope.Default).provider {
            LaunchXapiUseCaseAndroid(
                androidContext = this@AppActivity,
                endpoint = context,
                resolveXapiLaunchHrefUseCase = instance(),
                lightToolbarColor = md_theme_light_primaryContainer.toArgb(),
                darkToolbarColor = md_theme_dark_primaryContainer.toArgb(),
                session = { mCustomTabsSession },
                getHtmlContentDisplayEngineUseCase = instance(),
                embeddedHttpServer = instance(),
            )
        }
    }

    /*
     * Custom tabs setup as per https://developer.chrome.com/docs/android/custom-tabs/guide-warmup-prefetch
     *
     * As per:
     * https://developer.chrome.com/docs/android/custom-tabs#when_should_i_use_custom_tabs
     * "Lifecycle management: Apps launching a Custom Tab won't be evicted by the system during the Tabs use - its importance is raised to the "foreground" level."
     *
     * The objective here isn't so much about tracking engagement, it is just to ensure that the
     * app itself is kept alive when the user is in the custom tab (which will rely on loading
     * content from the embedded server).
     */
    private val customTabCallback = object: CustomTabsCallback() {
        override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
            super.onNavigationEvent(navigationEvent, extras)
            Napier.d { "CustomTab: NavigationEvent: $navigationEvent" }
        }
    }

    private val mCustomTabsServiceConnection = object: CustomTabsServiceConnection() {

        override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
            Napier.d { "CustomTab: Service connected" }
            mCustomTabsClient = client
            Napier.d { "CustomTab: Warmup" }
            client.warmup(0)
            mCustomTabsSession = client.newSession(customTabCallback)
            Napier.d { "CustomTab: Session created" }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Napier.d { "CustomTabs: Service disconnected" }
            mCustomTabsClient = null
            mCustomTabsSession = null

            /*
             * As per
             * https://developer.chrome.com/docs/android/custom-tabs/guide-warmup-prefetch
             * The custom tab connection might fail, so we need to reconnect if/when that happens.
             */
            lifecycleScope.launch {
                Napier.d { "CustomTab: disconnected, launching reconnection after 500ms" }
                delay(1_000)
                bindCustomTabsService()
            }
        }
    }

    private fun bindCustomTabsService() {
        if(mCustomTabsClient != null) {
            //do nothing
            return
        }

        /**
         * Where the default browser does not support the custom tab service, we need to provide
         * package names. On older Xiaomi devices their own browser is set as default, and it does not
         * support Custom Chrome Tabs. This causes getPackageName to return null. When the intent
         * to open a url is launched, it ignores the custom chrome tab extras and just opens the
         * link as normal (including address bar etc).
         *
         * We therefor need to provide the package names of well known browsers (Chrome and Firefox)
         * that properly support custom tabs.
         */
        val packageName = CustomTabsClient.getPackageName(
            this, listOf("com.android.chrome", "org.mozilla.firefox"), false
        )
        if(packageName == null) {
            Napier.w("CustomTabs: Service NOT supported")
            return
        }
        CustomTabsClient.bindCustomTabsService(this, packageName, mCustomTabsServiceConnection)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Official docs make no mention of disconnecting, does not seem to be required
        // https://developer.chrome.com/docs/android/custom-tabs/guide-warmup-prefetch
        bindCustomTabsService()
    }

    override fun onLocalesChanged(locales: LocaleListCompat) {
        super.onLocalesChanged(locales)

        //App must be fully restart when locale changes - see SetLanguageUseCaseAndroid for details.
        ProcessPhoenix.triggerRebirth(this@AppActivity)
    }

    override fun onDestroy() {
        Napier.d { "AppActivity#onDestroy" }
        super.onDestroy()
    }

}