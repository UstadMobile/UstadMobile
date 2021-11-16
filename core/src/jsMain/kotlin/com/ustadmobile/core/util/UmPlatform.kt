package com.ustadmobile.core.util

import kotlin.js.Date

actual object UmPlatform {

    actual var isWeb: Boolean = true

    actual fun debug(){
        js("debugger;")
    }

    actual fun console(content: Any) {
        console.log(Date(),content)
    }

}