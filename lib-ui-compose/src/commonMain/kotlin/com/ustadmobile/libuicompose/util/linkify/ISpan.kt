package com.ustadmobile.libuicompose.util.linkify

sealed class ISpan(
    val beginIndex: Int,
    val endIndex: Int,
)

class LinkISpan(beginIndex: Int, endIndex: Int) : ISpan(beginIndex, endIndex)

class TextISpan(beginIndex: Int, endIndex: Int): ISpan(beginIndex, endIndex)



