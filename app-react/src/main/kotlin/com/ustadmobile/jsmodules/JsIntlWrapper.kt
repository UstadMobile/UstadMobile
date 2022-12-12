package com.ustadmobile.jsmodules

import kotlin.js.Date

/**
 * Wrapper for required Intl functions
 *
 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Intl/DateTimeFormat
 */
external class Intl {

    companion object {

        class DateTimeFormat(locale: String = definedExternally) {

            fun format(date: Date): String

        }

    }
}