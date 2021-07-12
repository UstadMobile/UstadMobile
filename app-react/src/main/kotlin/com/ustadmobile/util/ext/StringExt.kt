package com.ustadmobile.util.ext

import kotlin.text.toCharArray

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