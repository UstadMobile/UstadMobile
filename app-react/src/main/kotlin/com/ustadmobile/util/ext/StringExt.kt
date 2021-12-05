package com.ustadmobile.util.ext

import com.ustadmobile.util.urlSearchParamsToMap

/**
 * Format string as equivalent to String.format in android
 */
fun String.format(vararg args: Any): String{
    var placeHolder = this
    val isFloatInterpolation = this.contains("f")
    val charVar = (if(isFloatInterpolation) "f" else "%").toCharArray().first()
    this.filter { it == charVar}.forEachIndexed { index, _ ->
        val replaceTo = if(args.isNotEmpty() && args.size >= index) args[index].toString() else null
        val replaceFromPrefix = when(isFloatInterpolation){
            true -> "%${index + 1}.0"
            else -> """%${index + 1}${"$"}"""
        }
        if(replaceTo != null){
            placeHolder = placeHolder
                .replace("${replaceFromPrefix}d",replaceTo)
                .replace("${replaceFromPrefix}d%",replaceTo)
                .replace("${replaceFromPrefix}s",replaceTo)
                .replace("${replaceFromPrefix}s%",replaceTo)
                .replace("${replaceFromPrefix}f%",replaceTo)
        }
    }
    return placeHolder
}

fun String.joinString(vararg args: Any): String {
    return "$this ${args.joinToString(" ")}"
}

fun String.clean(): String{
    return this.replace("\\", "")
}

/**
 * Breaks content into words
 */
fun String.wordBreakLimit(numOfWords: Int = 10): String{
    val words = this.split("\\s+".toRegex()).map { word ->
        word.replace("""^[,\.]|[,\.]$""".toRegex(), "")
    }
    return words.take(if(words.size < numOfWords) words.size else numOfWords)
        .joinToString(" ") + if(words.size > numOfWords) "..." else ""
}

/**
 * Convert query params string to argument map
 */
fun String.toArgumentsMap(): Map<String, String>{
    return urlSearchParamsToMap(this)
}
