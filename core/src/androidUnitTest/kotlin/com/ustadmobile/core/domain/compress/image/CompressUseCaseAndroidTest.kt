package com.ustadmobile.core.domain.compress.image

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.ustadmobile.core.domain.compress.CompressParams
import org.junit.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = intArrayOf(Build.VERSION_CODES.TIRAMISU))
class CompressUseCaseAndroidTest {

    @JvmField
    @Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun givenInputImage_whenInvoked_thenWillCompress(){
        val context = getApplicationContext<Context>()
        val tmpFile = tempFolder.newFile()
        val outFile = tempFolder.newFile()
        javaClass.getResourceAsStream("/image/testfile1.png")!!.use { assetIn ->
            tmpFile.outputStream().use { fileOut ->
                assetIn.copyTo(fileOut)
                fileOut.flush()
            }
        }

        val compressUseCase = CompressImageUseCaseAndroid(context)
        runBlocking {
            val result = compressUseCase(
                fromUri = tmpFile.toUri().toString(),
                toUri = outFile.toUri().toString(),
                params = CompressParams(
                    maxWidth = 300,
                    maxHeight = 300,
                )
            )
            val resultUri = Uri.parse(result.uri)
            val resultFile = resultUri.toFile()
            assertTrue(resultUri.toFile().exists())
            assertTrue(resultFile.length() > 0)
        }
    }

}