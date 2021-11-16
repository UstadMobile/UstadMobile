package com.ustadmobile.core.util

actual object UmPlatform {

    actual var isWeb: Boolean = false

    actual fun debug(){}

    actual fun console(content: Any) {}

}