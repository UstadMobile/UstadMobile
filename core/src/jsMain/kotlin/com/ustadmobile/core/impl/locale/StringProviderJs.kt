package com.ustadmobile.core.impl.locale

import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.provider.JsStringProvider

class StringProviderJs(
    private val locale: String,
    private val jsStringProvider: JsStringProvider
) : StringProvider{

    override fun get(stringResource: StringResource): String {
        return stringResource.localized(jsStringProvider, locale)
    }

}