package com.ustadmobile.sharedse.security

import java.security.MessageDigest

actual fun getMessageDigestInstance(algorithm: String) = MessageDigest.getInstance(algorithm)

actual typealias MessageDigestSe = java.security.MessageDigest