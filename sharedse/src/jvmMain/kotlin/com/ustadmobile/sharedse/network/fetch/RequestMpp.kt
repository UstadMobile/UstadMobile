package com.ustadmobile.sharedse.network.fetch

actual open class RequestMpp actual constructor(url: String, file: String) {

    actual val url: String = url
        get() = field

    actual val file: String = file
        get() = field

}