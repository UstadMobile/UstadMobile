package com.ustadmobile.core.hooks

import com.ustadmobile.core.components.DIContext
import com.ustadmobile.core.impl.locale.StringProviderJs
import org.kodein.di.direct
import org.kodein.di.instance
import react.useMemo
import react.useRequiredContext

fun useStringProvider(): StringProviderJs {
    val di = useRequiredContext(DIContext)

    val stringProvider: StringProviderJs = di.direct.instance()
    return useMemo(dependencies = emptyArray()) {
        stringProvider
    }

}