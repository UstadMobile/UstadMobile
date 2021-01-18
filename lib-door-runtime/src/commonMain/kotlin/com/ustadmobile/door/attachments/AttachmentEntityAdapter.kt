package com.ustadmobile.door.attachments

interface AttachmentEntityAdapter<T> {

    fun getUri(entity: T): String

    fun setUri(entity: T, uri: String)

    fun getMd5(entity: T): String

    fun setMd5(entity: T, md5: String)

    fun getLength(entity: T): Int

    fun setLength(entity: T, length: Int)

}