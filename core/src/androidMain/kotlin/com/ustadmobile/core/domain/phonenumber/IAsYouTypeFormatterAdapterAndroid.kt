package com.ustadmobile.core.domain.phonenumber

import io.michaelrocks.libphonenumber.android.AsYouTypeFormatter

class IAsYouTypeFormatterAdapterAndroid(
    private val formatter: AsYouTypeFormatter
): IAsYouTypeFormatter {
    override fun clear() {

        formatter.clear()
    }

    override fun inputDigitAndRememberPosition(nextChar: Char): String {
        return formatter.inputDigitAndRememberPosition(nextChar)
    }

    override fun inputDigit(nextChar: Char): String {
        return formatter.inputDigit(nextChar)
    }
}


fun AsYouTypeFormatter.asIAsYouTypeFormatter() = IAsYouTypeFormatterAdapterAndroid(this)
