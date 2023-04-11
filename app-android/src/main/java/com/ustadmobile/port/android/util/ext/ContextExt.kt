package com.ustadmobile.port.android.util.ext

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.PREFKEY_LOCALE
import com.ustadmobile.core.impl.UstadMobileSystemImpl

//As per https://github.com/android/android-ktx/issues/363

fun Context.getActivityContext(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> this.baseContext.getActivityContext()
    else -> throw IllegalArgumentException("Not an activity context")
}

fun Context.getContextSupportFragmentManager(): FragmentManager {
    return (getActivityContext() as AppCompatActivity).supportFragmentManager
}

/**
 * Gets the locale setting from the main shared preferences. This is provided in addition to
 * systemImpl's common function because it is needed in onAttachBaseContext (which executes before
 * DI is ready).
 */
fun Context.getUstadLocaleSetting(): String {
    val sharedPrefs = getSharedPreferences(
        UstadMobileSystemImpl.APP_PREFERENCES_NAME, Context.MODE_PRIVATE)
    return sharedPrefs.getString(PREFKEY_LOCALE,
        UstadMobileSystemCommon.LOCALE_USE_SYSTEM) ?: UstadMobileSystemCommon.LOCALE_USE_SYSTEM

}
