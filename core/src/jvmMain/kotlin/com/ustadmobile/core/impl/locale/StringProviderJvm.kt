package com.ustadmobile.core.impl.locale

import dev.icerock.moko.resources.StringResource
import java.util.Locale

class StringProviderJvm(private val locale: Locale) : StringProvider{

    override fun get(stringResource: StringResource): String {
        return stringResource.localized(locale)
    }

}