package com.ustadmobile.core.domain.compress.audio

import android.content.Context
import android.media.MediaCodecList
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import com.ustadmobile.core.uri.UriHelperAndroid
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CompressAudioUseCaseAndroidTest {


    @JvmField
    @Rule
    var tempFolder = TemporaryFolder()

    @Test
    fun initTest() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val codecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val audioEncoders = codecList.codecInfos.filter { codecInfo ->
            codecInfo.isEncoder && codecInfo.supportedTypes.any { it.startsWith("audio") }
        }
        println(audioEncoders.flatMap { it.supportedTypes.toList() }.joinToString())

        val compressAudioUseCase = CompressAudioUseCaseAndroid(
            appContext = context,
            uriHelper = UriHelperAndroid(context)
        )
        val audioFile = tempFolder.newFile()
        this::class.java.getResourceAsStream("/river.mp3")!!.use { inStream ->
            audioFile.outputStream().use {
                inStream.copyTo(it)
                it.flush()
            }
        }

        val result = runBlocking {
            compressAudioUseCase(
                fromUri = audioFile.toUri().toString(),
            )
        }

        Assert.assertNotNull(result)

        val resultFile = DoorUri.parse(result!!.uri).toFile()
        assertTrue(resultFile.exists())
        assertTrue(resultFile.length() > 0)
        println("result for is ${resultFile.length()}")
    }


}