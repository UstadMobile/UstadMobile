package com.ustadmobile.port.android.impl

import kotlinx.coroutines.channels.Channel

/**
 * This is a truly horrible, awful, workaround that is required. See SetLanguageUseCaseAndroid
 * for details on why.
 *
 * A loop on UstadApp checks for runtime locale changes. This could be done internally via the app,
 * or it could be done by app language settings on Android 13+. OnConfigChange and onLocaleChange
 * are simply not called (might be an official bug). When a locale change is detected, the new
 * locale tags are emitted on the channel. This is then collected (once and only once) by the
 * AppActivity, so that ProcessPheonix can trigger "rebirth" as required to avoid the onAppStateChanged
 * bug.
 *
 * Thanks, Google.
 */
interface UstadLocaleChangeChannelProvider {

    val localeChangeChannel: Channel<String>

}