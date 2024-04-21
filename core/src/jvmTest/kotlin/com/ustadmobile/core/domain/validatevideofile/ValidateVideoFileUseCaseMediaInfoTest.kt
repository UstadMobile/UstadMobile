package com.ustadmobile.core.domain.validatevideofile

import com.ustadmobile.core.domain.cachestoragepath.GetStoragePathForUrlUseCase
import com.ustadmobile.core.domain.compress.CompressionType
import com.ustadmobile.core.domain.extractmediametadata.mediainfo.ExecuteMediaInfoUseCase
import com.ustadmobile.core.domain.extractmediametadata.mediainfo.ExtractMediaMetadataUseCaseMediaInfo
import com.ustadmobile.door.ext.toDoorUri
import com.ustadmobile.util.test.ext.newFileFromResource
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidateVideoFileUseCaseMediaInfoTest {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var validatorUseCase: ValidateVideoFileUseCase

    private lateinit var executeMediaInfoUseCase: ExecuteMediaInfoUseCase

    @BeforeTest
    fun setup() {
        val mockGetStoragePathForUrlUseCase: GetStoragePathForUrlUseCase  = mock {
            onBlocking { invoke(any(), any(), any(), any()) }.thenAnswer { invocation ->
                GetStoragePathForUrlUseCase.GetStoragePathResult(
                    fileUri = invocation.arguments.first() as String,
                    compression = CompressionType.NONE,
                )
            }
        }

        executeMediaInfoUseCase = ExecuteMediaInfoUseCase(
            mediaInfoPath = "/usr/bin/mediainfo",
            workingDir = temporaryFolder.newFolder(),
            json = Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            },
        )

        validatorUseCase = ValidateVideoFileUseCase(
            extractMediaMetadataUseCase = ExtractMediaMetadataUseCaseMediaInfo(
                executeMediaInfoUseCase = executeMediaInfoUseCase,
                getStoragePathForUrlUseCase = mockGetStoragePathForUrlUseCase
            ),
        )
    }

    @Test
    fun givenValidVideo_whenInvoked_willReturnTrue() {
        val videoFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/BigBuckBunny.mp4")


        runBlocking {
            assertTrue(validatorUseCase(videoFile.toDoorUri()))
        }
    }

    @Test
    fun givenFileIsNotVideo_whenInvoked_willReturnFalse() {
        val otherFile = temporaryFolder.newFileFromResource(this::class.java,
            "/com/ustadmobile/core/container/testfile1.png")
        runBlocking {
            assertFalse(validatorUseCase(otherFile.toDoorUri()))
        }
    }

    @Test
    fun givenFileDoesNotExist_whenInvoked_willReturnFalse() {
        val fileNotExisting = File("idontexist")
        runBlocking {
            assertFalse(validatorUseCase(fileNotExisting.toDoorUri()))
        }
    }

}