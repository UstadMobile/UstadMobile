@file:OptIn(ExperimentalStdlibApi::class)

package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.viewmodel.UstadViewModel.Companion.ARG_SELECTED_ACCOUNT_ENDPOINT_URL
import com.ustadmobile.core.viewmodel.UstadViewModel.Companion.ARG_SELECTED_ACCOUNT_PERSON_UID
import com.ustadmobile.door.ext.hexStringToByteArray
import kotlin.text.Typography.ellipsis


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

/**
 * If this string is a hex string, convert it to base64
 */
fun String.hexStringToBase64Encoded(): String = hexStringToByteArray().encodeBase64()

/**
 * If this string is a base64 string, convert it to hex
 */
@OptIn(ExperimentalStdlibApi::class)
fun String.base64EncodedToHexString(): String = base64StringToByteArray().toHexString()

fun String.truncate(
    maxLength: Int = 24,
    appendIfTruncated: Char? = ellipsis,
): String{
    return if(this.length > maxLength) {
        this.substring(0, maxLength).let {
            if(appendIfTruncated != null)
                it.plus(appendIfTruncated)
            else
                it
        }
    } else{
        this
    }
}

/**
 * Check if the current string starts with https:// or http://
 */
fun String.startsWithHttpProtocol(): Boolean = lowercase().let {
    it.startsWith("http://") || it.startsWith("https://")
}

fun String.requireHttpPrefix(defaultProtocol: String = "https"): String {
    return if(startsWithHttpProtocol())
        this
    else
        "$defaultProtocol://$this"
}

/**
 * Where this string is a URI of some kind, append query arguments to it. If the string
 * already contains a ?, then the arguments will be appended after an &amp;
 * Otherwise, a ? will be added and then the query args
 */
@Suppress("LiftReturnOrAssignment") //This is wrong: still needs to append queryArgs
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
 * Adds ARG_SELECTED_ACCOUNT_PERSON_UID and ARG_SELECTED_ACCOUNT_ENDPOINT_URL to the given string
 * Useful where ARG_NEXT needs to have the selected account information appended.
 */
fun String.appendSelectedAccount(personUid: Long, endpoint: Endpoint) : String {
    return appendQueryArgs(
        mapOf(
            ARG_SELECTED_ACCOUNT_PERSON_UID to personUid.toString(),
            ARG_SELECTED_ACCOUNT_ENDPOINT_URL to endpoint.url,
        )
    )
}


/**
 * Where this string is a URI of some kind, append query arguments to it. If the string
 * already contains a ?, then the arguments will be appended after an &amp;
 * Otherwise, a ? will be added and then the query args
 */
fun String.appendQueryArgs(args: Map<String, String>): String {
    return if(args.isNotEmpty()) {
        appendQueryArgs(args.toQueryString())
    }else {
        this
    }
}

fun String.capitalizeFirstLetter(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

//Could be done with a regex, but this can be used on long text very frequently, so
//this is better for performance
fun String.countWords(): Int {
    var lastCharWasWhitespace = true

    var wordCount = 0
    for(i in 0 until length) {
        val thisCharWhitespace = this[i].isWhitespace()
        if(!thisCharWhitespace && lastCharWasWhitespace)
            wordCount++

        lastCharWasWhitespace = thisCharWhitespace
    }

    return wordCount
}

fun String.initial(): String {
    return firstNonWhiteSpaceChar()?.uppercase() ?: ""
}

/**
 * Remove excess (more than one) white space from the start and end of a string. Will leave up to
 * one white space character on either end.
 */
fun String.trimExcessWhiteSpace() : String {
    val firstNonWhiteSpace = indexOfFirst { !it.isWhitespace() }
    val lastNonWhiteSpace = indexOfLast { !it.isWhitespace() }
    val startPos = kotlin.math.max(0, firstNonWhiteSpace - 1)
    val endPos = kotlin.math.min(length, lastNonWhiteSpace + 2) //Add 2 because endpos is exclusive

    return if(startPos != 0 || endPos != length) {
        substring(startPos, endPos)
    }else{
        this
    }
}

fun String.fileExtensionOrNull(): String? {
    return substringAfterLast(".", "").let {
        if(it != "") it else null
    }
}

fun String.removeQueryStringSuffix(): String {
    val queryPosIndex = indexOf("?")
    return if(queryPosIndex != -1) {
        substring(0, queryPosIndex)
    }else {
        this
    }
}

fun String.removeHashSuffix() : String {
    return substringBefore("#")
}

fun String.removeFileExtension() : String {
    return substringBeforeLast(".")
}

fun String.displayFilename(
    removeExtension: Boolean = true
): String {
    val base = substringAfterLast("/")
        .substringAfterLast("\\")
        .substringBefore("?")
    return if(removeExtension)
        base.removeFileExtension()
    else
        base
}


fun String.toNullIfBlank(): String? = if(isBlank()) { null } else { this }

fun String.substringUntilLastIndexOfInclusive(
    delimiter: String,
    missingDelimiterValue: String = this
) : String {
    val index = lastIndexOf(delimiter)
    return if(index != -1) {
        substring(0, index + delimiter.length)
    }else {
        missingDelimiterValue
    }
}

fun String.firstNonWhiteSpaceChar(): Char? {
    val index = indexOfFirst { !it.isWhitespace() }
    return if(index != -1)
        this[index]
    else
        null
}

fun String?.ifNullOrBlank(defaultValue: () -> String): String {
    return this?.ifBlank(defaultValue) ?: defaultValue()
}
