package com.ustadmobile.core.util.ext

actual fun String.base64StringToByteArray(): ByteArray = TODO("Not implemented on JS yet")

actual fun String?.validEmail(): Boolean {
    val value = this
    val valid =  js("/^(([^<>()[\\]\\\\.,;:\\s@\\\"]+(\\.[^<>()[\\]\\\\.,;:\\s@\\\"]+)*)|(\\\".+\\\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))\$/.test(value)")
    return valid.unsafeCast<Boolean>()
}