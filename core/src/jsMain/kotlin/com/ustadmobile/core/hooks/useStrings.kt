package com.ustadmobile.core.hooks

import com.ustadmobile.core.components.DIContext
import com.ustadmobile.core.impl.locale.JsStringXml
import com.ustadmobile.core.impl.locale.StringsXml
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import react.useContext
import react.useMemo

fun useStringsXml(): StringsXml {

    val di = useContext(DIContext)

    val stringsXml = di.direct.instanceOrNull<StringsXml>(tag = JsStringXml.DISPLAY) ?:
        di.direct.instance<StringsXml>(tag = JsStringXml.DEFAULT)

    return useMemo(dependencies = emptyArray()) {
        stringsXml
    }

}