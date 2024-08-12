

package com.ustadmobile.core.wrappers.compressorjs

import web.blob.Blob

//Declarations are as per https://www.npmjs.com/package/compressorjs
@Suppress("unused")
external interface CompressorOptions {

    var strict: Boolean

    var checkOrientation: Boolean

    var retainExif: Boolean

    var maxWidth: Int

    var maxHeight: Int

    var minWidth: Int

    var minHeight: Int

    var width: Int

    var height: Int

    var resize: String

    var quality: Float

    var mimeType: String

    //String or array of strings as per https://www.npmjs.com/package/compressorjs#converttypes
    var convertTypes: Any

    var convertSize: Int

    var success: (Blob) -> Unit

    var error: (Error) -> Unit
}

//Declarations are as per https://www.npmjs.com/package/compressorjs
@Suppress("unused")
@JsModule("compressorjs")
@JsName("default")
external class Compressor(blob: Blob, options: CompressorOptions = definedExternally) {
    fun abort()
}

