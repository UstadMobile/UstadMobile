package com.ustadmobile.core.util

actual object UmPlatform {

    actual var isWeb: Boolean = true

    actual fun debug() {
        js("debugger")
    }

}