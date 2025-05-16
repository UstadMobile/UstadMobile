package com.ustadmobile.core.domain.credentials.passkey

import com.ustadmobile.core.account.LearningSpace

interface DecodeUserHandleUseCase {
    operator fun invoke(encodedHandle: String): Pair<LearningSpace, Long>
}