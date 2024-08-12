package com.ustadmobile.hooks

import com.ustadmobile.mui.components.UstadLanguageConfigContext
import com.ustadmobile.wrappers.intl.Intl
import com.ustadmobile.wrappers.intl.IntlDateTimeNumericProp
import js.objects.jso
import react.useContext
import react.useMemo

fun useTimeFormatter(): Intl.Companion.DateTimeFormat {
    val langConfig = useContext(UstadLanguageConfigContext)
    return useMemo(dependencies = emptyArray()) {
        Intl.Companion.DateTimeFormat(langConfig?.displayedLocale ?: "en", jso {
            hour = IntlDateTimeNumericProp.numeric
            minute = IntlDateTimeNumericProp.numeric
        })
    }
}
