package com.ustadmobile.core.domain.compress.list

import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressResult
import com.ustadmobile.core.domain.compress.video.CompressVideoUseCase
import com.ustadmobile.core.io.ext.toDoorUri
import com.ustadmobile.libcache.headers.FileMimeTypeHelperImpl
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class CompressListUseCaseTest {

    @Test
    fun givenFileList_whenInvoked_thenWillInvokeCompressorAndReturnResult() {
        val compressedFileMockUri  = "file:///some/path"
        val mockVideoCompressor: CompressVideoUseCase = mock {
            onBlocking { invoke(any(), anyOrNull(), any(), any()) }.thenReturn(
                CompressResult(
                    compressedFileMockUri, "video/mp4", 2_000_000, 1_000_000,
                )
            )
        }

        val compressListUseCase = CompressListUseCase(
            compressVideoUseCase = mockVideoCompressor,
            mimeTypeHelper = FileMimeTypeHelperImpl(),
            compressImageUseCase = null,
            filesystem = SystemFileSystem,
        )

        runBlocking {
            val fileList = listOf(
                CompressListUseCase.ItemToCompress(
                    path = Path("path/to/video-webm"),
                    name = "file.webm",
                    mimeType = null,
                ),
                CompressListUseCase.ItemToCompress(
                    path = Path("path/to/file.txt"),
                    name = "file.txt",
                    mimeType = null,
                )
            )

            val compressParam = CompressParams()

            val result = compressListUseCase(
                items = fileList,
                params = compressParam,
                workDir = Path("dummy"),
            )

            verifyBlocking(mockVideoCompressor) {
                invoke(eq(fileList.first().path.toDoorUri().toString()), anyOrNull(), eq(compressParam), anyOrNull())
            }

            assertEquals(
                compressedFileMockUri, result.first().compressedResult?.uri,
                "Compress result returns uri from compressor"
                )
        }
    }

}