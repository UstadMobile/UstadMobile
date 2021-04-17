package com.ustadmobile.util.ext

fun String.format(vararg args: Any): String{
    var placeHolder = this
    this.match("\\d+")?.forEachIndexed { index, arg ->
        val replaceTo = if(args.isNotEmpty() && args.size >= index) args[index].toString() else null
        val replaceFromPrefix = """%$arg${"$"}"""
        if(replaceTo != null){
            placeHolder = placeHolder.replace("${replaceFromPrefix}d",replaceTo)
                .replace("${replaceFromPrefix}s",replaceTo)
        }
    }
    return placeHolder
}

fun String.joinString(vararg args: Any): String {
    return "$this ${args.joinToString(" ")}"
}