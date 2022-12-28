package com.ustadmobile.jsmodules

import com.ustadmobile.wrappers.intl.Intl
import kotlin.js.Date
import kotlin.test.Test

class JsIntlWrapperTest {

    @Test
    fun givenValidLocale_whenFormatCalled_thenShouldFormatDate() {
        console.log("Now: ${Intl.Companion.DateTimeFormat("en").format(Date())}")
    }

}