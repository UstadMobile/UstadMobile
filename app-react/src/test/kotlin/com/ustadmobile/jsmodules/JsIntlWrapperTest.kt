package com.ustadmobile.jsmodules

import kotlin.js.Date
import kotlin.test.Test

class JsIntlWrapperTest {

    @Test
    fun givenValidLocale_whenFormatCalled_thenShouldFormatDate() {
        console.log("Now: ${Intl.Companion.DateTimeFormat("en").format(Date())}")
    }

}