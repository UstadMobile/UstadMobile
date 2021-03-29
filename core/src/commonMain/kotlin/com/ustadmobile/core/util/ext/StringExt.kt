package com.ustadmobile.core.util.ext

import com.ustadmobile.door.ext.hexStringToByteArray
import com.ustadmobile.door.ext.toHexString
import kotlin.text.Typography.ellipsis


fun String.inBrackets() = "($this)"

expect fun String.base64StringToByteArray(): ByteArray

fun String?.toQueryLikeParam() = if(this.isNullOrEmpty()) "%" else "%$this%"

inline fun String.requirePostfix(postFix: String) = if(this.endsWith(postFix)) this else "$this$postFix"

fun String?.alternative(alternative: String) = if(this.isNullOrEmpty()) alternative else this

/**
 * If this string is a hex string, convert it to base64
 */
fun String.hexStringToBase64Encoded(): String = hexStringToByteArray().encodeBase64()

/**
 * If this string is a base64 string, convert it to hex
 */
fun String.base64EncodedToHexString(): String = base64StringToByteArray().toHexString()

fun String.truncate(maxLength: Int = 24): String{
    return if(this.length > maxLength) {
        this.substring(0, maxLength).plus(ellipsis)
    } else{
        this
    }
}