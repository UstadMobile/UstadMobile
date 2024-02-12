package com.ustadmobile.core.domain.contententry.launchcontent

import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.lib.db.entities.ContentEntryVersion

/**
 * UseCase for opening content represented by a ContentEntryVersion. Normally this is done by
 * simply navigating to a screen for this type of Content e.g. XapiContentScreen etc.
 *
 * Sometimes we cannot (or don't want to) directly open content in a "normal" viewmodel screen e.g.
 *   Xapi on Android: issues with the WebView on Android make the display of some content items
 *   unreliable so we use the ChromeTabs API and load via the embedded server (to support offline
 *   access).
 *   Xapi and Ebooks on Desktop: there isn't a good webview option, so we run show content using
 *   the chrome command using the --app argument (to hide the address bar etc) and load via
 *   the embedded server (to support offline access).
 *
 * The above implementations can use the underlying platform mechanisms to launch external apps.
 */
interface LaunchContentEntryVersionUseCase {


    data class LaunchResult(
        val message: String? = null
    )

    suspend operator fun invoke(
        contentEntryVersion: ContentEntryVersion,
        navController: UstadNavController,
    ): LaunchResult?


}