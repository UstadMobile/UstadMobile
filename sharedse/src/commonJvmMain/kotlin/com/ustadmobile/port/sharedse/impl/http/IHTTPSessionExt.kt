package com.ustadmobile.port.sharedse.impl.http
import fi.iki.elonen.NanoHTTPD.Method
import fi.iki.elonen.NanoHTTPD
import java.io.File

inline fun <reified T> NanoHTTPD.IHTTPSession.parseRequestBody() : T?{
    if(T::class == String::class) {
        val bodyMap = mutableMapOf<String,String>()
        parseBody(bodyMap)

        if(this.method == Method.PUT) {
            //NanoHTTPD will always put the content of a PUT body into a temp file, with the path in the "content" key
            val tmpFileName = bodyMap["content"]
            return tmpFileName?.let { File(it).readText() } as T
        }else if(this.method == Method.POST) {
            //NanoHTTPD will put small (less than 1024 bytes) content into the memory, otherwise it will make a file
            val mapContent = bodyMap["postData"] ?: return null
            val tmpFile = File(mapContent)
            if(tmpFile.exists()) {
                return tmpFile.readText() as T
            }else {
                return mapContent as T
            }
        }else {
            return null
        }
    }else {
        throw kotlin.IllegalArgumentException("Invalid return type expected")
    }
}
