package com.ustadmobile.hooks

import com.ustadmobile.core.impl.locale.StringProviderJs
import com.ustadmobile.core.impl.locale.StringResourceUiText
import com.ustadmobile.core.impl.locale.StringUiText
import com.ustadmobile.core.impl.locale.UiText

fun uiText(uiText: UiText, stringProvider: StringProviderJs) : String {
    return when(uiText) {
        is StringUiText -> uiText.text
        is StringResourceUiText -> stringProvider.get(uiText.resource)
    }
}
