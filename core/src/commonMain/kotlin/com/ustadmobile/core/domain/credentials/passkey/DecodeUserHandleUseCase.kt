package com.ustadmobile.core.domain.credentials.passkey

import com.ustadmobile.core.account.LearningSpace

/**
 * Decode a user handle encoded by EncodeUserHandleUseCase - see EncodeUserHandleUseCase
 */
interface DecodeUserHandleUseCase {

    operator fun invoke(encodedHandle: String): Pair<LearningSpace, Long>

}