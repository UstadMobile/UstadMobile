package com.ustadmobile.util.ext

import kotlin.text.toCharArray

fun String.format(vararg args: Any): String{
    var placeHolder = this
    this.filter { it == "%".toCharArray().first() }.forEachIndexed { index, _ ->
        val replaceTo = if(args.isNotEmpty() && args.size >= index) args[index].toString() else null
        val replaceFromPrefix = """%${index + 1}${"$"}"""
        if(replaceTo != null){
            placeHolder = placeHolder
                .replace("${replaceFromPrefix}d",replaceTo)
                .replace("${replaceFromPrefix}s",replaceTo)
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