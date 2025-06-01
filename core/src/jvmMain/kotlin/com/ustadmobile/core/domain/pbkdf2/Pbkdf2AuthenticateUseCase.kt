package com.ustadmobile.core.domain.pbkdf2


class Pbkdf2AuthenticateUseCase(
    private val encryptUseCase: Pbkdf2EncryptUseCase,
) {

    operator fun invoke(
        password: String,
        encryptedPassword: ByteArray,
        salt: String,
        iterations: Int = 10_000,
        keyLength: Int = 512,
    ): Boolean {
        return encryptedPassword.contentEquals(
            encryptUseCase(password, salt, iterations, keyLength)
        )
    }

}