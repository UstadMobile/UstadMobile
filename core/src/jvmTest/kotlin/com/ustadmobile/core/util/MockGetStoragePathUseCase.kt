package com.ustadmobile.core.util

import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.core.domain.compress.CompressionType
import org.mockito.kotlin.any
import org.mockito.kotlin.mock

/**
 * Mock a GetStoragePathForUrlUseCase where we know that the actual input urls will always be
 * files
 */
fun mockGetStoragePathUseCase(): GetStoragePathForUrlUseCase  = mock {
    onBlocking { invoke(any(), any(), any(), any()) }.thenAnswer { invocation ->
        GetStoragePathForUrlUseCase.GetStoragePathResult(
            fileUri = invocation.arguments.first() as String,
            compression = CompressionType.NONE,
        )
    }
}
