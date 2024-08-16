


package com.ustadmobile.core.wrappers.xxhash

import com.ustadmobile.core.wrappers.cuint.UINT64
import js.buffer.ArrayBufferLike

@JsModule("xxhashjs")
@JsNonModule
external object XXH {

    fun h64(data: ArrayBufferLike, seed: Int = definedExternally): UINT64

    fun h64(): XXH64
}

external class XXH64 {

    fun init(seed: Number)

    fun update(data: ArrayBufferLike)

    fun digest(): UINT64

}
