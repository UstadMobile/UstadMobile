package com.ustadmobile.core.domain.compress.image

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import com.ustadmobile.core.domain.compress.CompressParams
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CompressImageUseCaseAndroidTest {

    @JvmField
    @Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun givenInputImage_whenInvoked_thenWillCompress(){
        val context = ApplicationProvider.getApplicationContext<Context>()
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
                    maxWidth = 800,
                    maxHeight = 800,
                )
            )
            val resultUri = Uri.parse(result!!.uri)
            val resultFile = resultUri.toFile()
            Assert.assertTrue(resultUri.toFile().exists())
            Assert.assertTrue(resultFile.length() > 0)
            Assert.assertTrue(resultFile.length() < tmpFile.length())
        }
    }

}