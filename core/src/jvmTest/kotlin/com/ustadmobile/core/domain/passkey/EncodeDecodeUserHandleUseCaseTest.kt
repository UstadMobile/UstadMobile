package com.ustadmobile.core.domain.passkey

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.util.ext.requirePostfix
import com.ustadmobile.core.util.ext.trimExcessWhiteSpace
import kotlin.test.Test
import kotlin.test.assertEquals

class EncodeDecodeUserHandleUseCaseTest {

    @Test
    fun givenPersonUidAndLearningSpace_whenEncodedAndThenDecoded_thenShouldReturnSameValues() {
        val originalUid = 727860624594890752

        val learningSpace = LearningSpace("http://192.168.1.35:8087")

        val encodeUseCase = EncodeUserHandleUseCaseImpl(learningSpace)
        val decodeUseCase = DecodeUserHandleUseCaseImpl()
        val encoded = encodeUseCase(originalUid)
        val (decodedLearningSpace, decodedUid) = decodeUseCase(encoded)
        assertEquals(originalUid, decodedUid)

        assertEquals(
            learningSpace.url,
            decodedLearningSpace.url.trimExcessWhiteSpace()
        )
    }
    @Test
    fun givenPersonUidAndLiveUrl_whenEncodedAndThenDecoded_thenShouldReturnSameValues() {
        val originalUid = 7272232323
        val learningSpace = LearningSpace("http://learning.tree.com")

        val encodeUseCase = EncodeUserHandleUseCaseImpl(learningSpace)
        val decodeUseCase = DecodeUserHandleUseCaseImpl()
        val encoded = encodeUseCase(originalUid)
        val (decodedLearningSpace, decodedUid) = decodeUseCase(encoded)
        assertEquals(originalUid, decodedUid)

        assertEquals(
            learningSpace.url,
            decodedLearningSpace.url.trimExcessWhiteSpace()
        )
    }
}
