package com.ustadmobile.util

import kotlinx.browser.window

object UmUtil {

    fun isDarkModeEnabled(): Boolean{
        return window.matchMedia("(prefers-color-scheme: dark)").matches
    }
}