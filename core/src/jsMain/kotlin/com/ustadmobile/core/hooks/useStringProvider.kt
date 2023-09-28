package com.ustadmobile.core.hooks

import com.ustadmobile.core.components.DIContext
import com.ustadmobile.core.impl.locale.StringProvider
import org.kodein.di.direct
import org.kodein.di.instance
import react.useMemo
import react.useRequiredContext

fun useStringProvider(): StringProvider {
    val di = useRequiredContext(DIContext)

    val stringProvider: StringProvider = di.direct.instance()
    return useMemo(dependencies = emptyArray()) {
        stringProvider
    }

}