package com.ustadmobile.util.ext

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.impl.locale.StringsXml

/**
 * Format string as equivalent to String.format in android
 */
fun String.format(vararg args: Any): String{
    var placeHolder = this
    val results: MutableList<String> = mutableListOf()
    var nextMatch:MatchResult? = "%\\d.\\df%|%\\d\\\$d%|%\\d\\\$d|%\\d\\\$s%|%\\d\\\$s".toRegex().find(this)
    do{
        if(nextMatch?.value != null){
            val range = IntRange(nextMatch.range.first, nextMatch.range.last)
            println()
            results.add(placeHolder.substring(range))
        }
        nextMatch = nextMatch?.next()
    }while(nextMatch?.value != null)
    results.forEachIndexed { index, part ->
        placeHolder = placeHolder.replace(part, args[index].toString())
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
 * Shorthand to add (optional) to a field title
 * e.g. "FieldName" -> "Field name (optional)"
 */
fun String.addOptionalSuffix(stringProvider: StringProvider): String {
    return "$this (${stringProvider.get(MR.strings.optional)})"
}
