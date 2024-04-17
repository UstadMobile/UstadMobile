package com.ustadmobile.core.domain.compress.video

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import com.ustadmobile.core.domain.compress.CompressParams
import com.ustadmobile.core.domain.compress.CompressionLevel
import com.ustadmobile.core.uri.UriHelperAndroid
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileOutputStream

class CompressVideoUseCaseAndroidTest {

    @JvmField
    @Rule
    var tempFolder = TemporaryFolder()

    lateinit var videoFile: File

    lateinit var useCase: CompressVideoUseCaseAndroid

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val uriHelper = UriHelperAndroid(context)
        useCase = CompressVideoUseCaseAndroid(context, uriHelper)

        videoFile = tempFolder.newFile("bus.mp4")
        this::class.java.getResourceAsStream("/bus.mp4")!!.use { assetIn ->
            FileOutputStream(videoFile).use { fileOut ->
                assetIn.copyTo(fileOut)
                fileOut.flush()
            }
        }
    }

    private fun compressAndAssert(compressionLevel: CompressionLevel) {
        runBlocking {
            val result = useCase(
                fromUri = videoFile.toUri().toString(),
                params = CompressParams(
                    compressionLevel = compressionLevel
                )
            )

            val outFile = Uri.parse(result!!.uri).toFile()
            Assert.assertTrue(outFile.length() > 0)
        }
    }

    @Test
    fun givenValidVideo_whenCompressLow_thenWillCompress() {
        compressAndAssert(CompressionLevel.LOW)
    }

    @Test
    fun givenValidVideo_whenCompressMedium_thenWillCompress() {
        compressAndAssert(CompressionLevel.MEDIUM)
    }

    @Test
    fun givenValidVideo_whenCompressHigh_thenWillCompress() {
        compressAndAssert(CompressionLevel.HIGH)
    }


}