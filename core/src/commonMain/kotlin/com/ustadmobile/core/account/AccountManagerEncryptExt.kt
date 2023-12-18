package com.ustadmobile.core.account

internal expect suspend fun AuthManager.doublePbkdf2Hash(password: String): ByteArray

/**
 * For some reason Kotlin/JS Base64 decoding does not behave as expected. Putting this in as
 * expect/actual allows the Kotlin/JS version to use the string returned by the server
 */
internal expect suspend fun AuthManager.doublePbkdf2HashAsBase64(password: String): String

internal expect suspend fun AuthManager.encryptPbkdf2(password: String): ByteArray

