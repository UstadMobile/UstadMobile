package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Pbkdf2Params

fun String.encryptWithPbkdf2(salt: String, pbkdf2Params: Pbkdf2Params): String =
    encryptWithPbkdf2(salt, pbkdf2Params.iterations, pbkdf2Params.keyLength)