package com.ustadmobile.core.util

actual object UmPlatform {

    actual var isWeb: Boolean = true

    actual fun debug(){
        js("debugger;")
    }

    actual fun console(content: Any) {
        console.log(JSON.stringify(content))
    }

}