package com.ustadmobile.core.viewmodel.person

fun String?.toFirstAndLastNameExt(): Pair<String, String> {
    val parts = this?.trim()?.split(Regex("[ .]"), limit = 2)
    val firstName = parts?.getOrNull(0).orEmpty()
    val lastName = parts?.getOrNull(1).orEmpty()
    return firstName to lastName
}