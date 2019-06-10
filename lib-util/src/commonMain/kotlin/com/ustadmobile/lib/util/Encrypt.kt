package com.ustadmobile.lib.util

expect fun encryptPassword(originalPassword: String): String

expect fun authenticateEncryptedPassword(providedPassword: String,
                                         encryptedPassword: String): Boolean

