package com.ustadmobile.core.util.ext

/**
 * Encyrpt the given string with Pbkdf2 (one way) encryption. This is useful for one-way password
 * encryption.
 *
 * For JS: See
 * https://stackoverflow.com/questions/34462316/replicating-java-password-hashing-code-in-node-js-pbkdf2withhmacsha1
 */
expect fun String.encryptWithPbkdf2(salt: String, iterations: Int = 10000, keyLength: Int = 512): ByteArray
