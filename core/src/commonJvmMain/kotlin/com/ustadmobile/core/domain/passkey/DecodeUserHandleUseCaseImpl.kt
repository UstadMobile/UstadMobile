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

        val bytesToRead = decodedBytes.size - 8
        val byteArray = ByteArray(bytesToRead)
        byteBuffer.get(byteArray)

        val learningSpaceUrl = byteArray.decodeToString()

        return Pair(LearningSpace(learningSpaceUrl), uid)
    }
}
