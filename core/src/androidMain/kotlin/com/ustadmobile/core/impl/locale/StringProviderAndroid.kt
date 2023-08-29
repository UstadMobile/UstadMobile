package com.ustadmobile.core.impl.locale

import android.content.Context
import dev.icerock.moko.resources.StringResource

class StringProviderAndroid(private val appContext: Context): StringProvider {
    override fun get(stringResource: StringResource): String {
        return stringResource.getString(appContext)
    }

}