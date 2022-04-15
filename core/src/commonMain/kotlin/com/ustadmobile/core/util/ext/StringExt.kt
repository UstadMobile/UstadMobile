package com.ustadmobile.core.util.ext

import com.ustadmobile.door.ext.hexStringToByteArray
import com.ustadmobile.door.ext.toHexString
import kotlin.text.Typography.ellipsis


fun String.inBrackets() = "($this)"

/**
 * To ensure consistency between JVM and Android, Base64 encoding
 * **must** be done with NO_WRAP
 */
expect fun String.base64StringToByteArray(): ByteArray

fun String?.toQueryLikeParam() = if(this.isNullOrEmpty()) "%" else "%$this%"

fun String.requirePostfix(
    postFix: String,
    ignoreCase: Boolean = false
) = if(this.endsWith(postFix, ignoreCase)) this else "$this$postFix"

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

/**
 * Check if the current string starts with https:// or http://
 */
fun String.startsWithHttpProtocol(): Boolean = toLowerCase().let {
    it.startsWith("http://") || it.startsWith("https://")
}

fun String.requireHttpPrefix(defaultProtocol: String = "https"): String {
    if(startsWithHttpProtocol())
        return this
    else
        return "$defaultProtocol://$this"
}

/**
 * Where this string is a URI of some kind, append query arguments to it. If the string
 * already contains a ?, then the arguments will be appended after an &amp;
 * Otherwise, a ? will be added and then the query args
 */
fun String.appendQueryArgs(queryArgs: String): String {
    var retVal = this
    if(this.contains("?"))
        retVal += "&"
    else
        retVal += "?"

    retVal += queryArgs

    return retVal
}

/**
 * Where this string is a URI of some kind, append query arguments to it. If the string
 * already contains a ?, then the arguments will be appended after an &amp;
 * Otherwise, a ? will be added and then the query args
 */
fun String.appendQueryArgs(vararg pairs: Pair<String, String>): String {
    return appendQueryArgs(mapOf(*pairs).toQueryString())
}

/**
 * Where this string is a URI of some kind, append query arguments to it. If the string
 * already contains a ?, then the arguments will be appended after an &amp;
 * Otherwise, a ? will be added and then the query args
 */
fun String.appendQueryArgs(args: Map<String, String>): String {
    return appendQueryArgs(args.toQueryString())
}

fun String.capitalizeFirstLetter(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

/**
 * Validate email address using regular expressions
 */
expect fun String?.validEmail(): Boolean

fun String.countWords(): Int {
    return Regex("""(\s+|(\r\n|\r|\n))""").findAll(this.trim()).count() + 1
}
