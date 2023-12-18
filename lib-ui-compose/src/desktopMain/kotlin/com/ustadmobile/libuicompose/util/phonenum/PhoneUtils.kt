package com.ustadmobile.libuicompose.util.phonenum


//As per https://developer.android.com/reference/android/telephony/PhoneNumberUtils

const val PAUSE = ','
const val WAIT = ';'
const val WILD = 'N'

private val dialable = (0..9).map { it.digitToChar() } + listOf('*', '#', '+')

private val nonSeparable = dialable + listOf(PAUSE, WAIT, WILD)

actual fun isNonSeparator(char: Char): Boolean {
    return char in nonSeparable
}

actual fun isReallyDialable(char: Char): Boolean {
    return char in dialable
}

