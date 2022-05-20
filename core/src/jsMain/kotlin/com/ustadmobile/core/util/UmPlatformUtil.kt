package com.ustadmobile.core.util

import kotlinx.coroutines.CoroutineScope

actual object UmPlatformUtil {

    actual var isWeb: Boolean = true

    actual fun run(block:() -> Unit){}

    actual suspend fun runAsync( block: suspend CoroutineScope.() -> Unit){}

    actual fun debug(){
        js("debugger;")
    }

    actual fun log(content: Any) {
        console.log("JS-LOG",JSON.stringify(content))
    }

    actual suspend fun runIfNotJsAsync(block: suspend () -> Unit){
    }

    actual fun runIfNotJs(block: () -> Unit) {
    }


}