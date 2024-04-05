package com.ustadmobile.core.domain.compress.video

import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressResult
import com.ustadmobile.core.domain.compress.CompressUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Compress a video using VLC command line
 */
class CompressVideoUseCaseJvmVlc: CompressUseCase {

    override suspend fun invoke(
        fromUri: String,
        toUri: String?,
        params: CompressParams,
        onProgress: CompressUseCase.OnCompressProgress?
    ): CompressResult? {
        withContext(Dispatchers.IO) {
            TODO()


        }
    }
}