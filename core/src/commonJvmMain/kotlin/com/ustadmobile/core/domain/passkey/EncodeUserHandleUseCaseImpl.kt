package com.ustadmobile.core.domain.passkey

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.credentials.passkey.EncodeUserHandleUseCase
import io.ktor.util.encodeBase64
import io.ktor.utils.io.core.toByteArray
import java.nio.ByteBuffer


class EncodeUserHandleUseCaseImpl(
    val learningSpace: LearningSpace
) : EncodeUserHandleUseCase {

    override fun invoke(
        personPasskeyUid: Long
    ): String {
        val stringToEncode = learningSpace.url
        val stringBytes = stringToEncode.toByteArray()

        if (stringBytes.size > 55) {
            throw IllegalArgumentException("Learning space URL is too long")
        }

        val byteBuffer = ByteBuffer.allocate(8 + stringBytes.size) // Clean: no unnecessary 64-byte array
        byteBuffer.putLong(personPasskeyUid)
        byteBuffer.put(stringBytes)

        return byteBuffer.array().encodeBase64()
    }
}