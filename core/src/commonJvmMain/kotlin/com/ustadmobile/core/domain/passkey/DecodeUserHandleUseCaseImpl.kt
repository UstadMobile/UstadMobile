package com.ustadmobile.core.domain.passkey

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.credentials.passkey.DecodeUserHandleUseCase
import com.ustadmobile.core.util.ext.base64StringToByteArray
import java.nio.ByteBuffer

class DecodeUserHandleUseCaseImpl : DecodeUserHandleUseCase {
    override operator fun invoke(
        encodedHandle: String
    ): Pair<LearningSpace, Long> {
        val decodedBytes = encodedHandle.base64StringToByteArray()
        val byteBuffer = ByteBuffer.wrap(decodedBytes)

        val uid = byteBuffer.long
        //drop 8 skips the first 8 bytes which is long used for personPasskeyUid.
        //.takeWhile { it != 0.toByte() } keeps bytes until it finds the first zero byte,
        // which marks the end of the string.
        val remainingBytes = decodedBytes.drop(8).takeWhile { it != 0.toByte() }.toByteArray()
        val learningSpacePart = remainingBytes.decodeToString().removePrefix("@").trim()

        return Pair(LearningSpace(learningSpacePart), uid)
    }
}

