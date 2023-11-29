package com.ustadmobile.core.domain.phonenumber

interface IAsYouTypeFormatter {

    fun clear()

    fun inputDigitAndRememberPosition(nextChar: Char): String

    fun inputDigit(nextChar: Char): String
}