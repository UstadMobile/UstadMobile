package com.ustadmobile.core.catalog.contenttype

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.door.DoorUri
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import java.io.FileOutputStream

class XapiContentTypePluginTest {

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    @Before
    fun setup(){
        di = DI {
            import(ustadTestRule.diModule)
        }

    }

    @Test
    fun givenValidTinCanFormatFile_whenGetContentEntryCalled_thenShouldReadMetaData() {
        val inputStream = this::class.java.getResourceAsStream(
                "/com/ustadmobile/core/contenttype/ustad-tincan.zip")
        val tempFile = temporaryFolder.newFile("tincan.zip")
        inputStream.use {inStream ->
            FileOutputStream(tempFile).use {
                inStream.copyTo(it)
                it.flush()
            }
        }

        val xapiPlugin =  XapiTypePluginCommonJvm(Any(), Endpoint("http://localhost/dummy"), di)
        val contentEntry = runBlocking {
            xapiPlugin.extractMetadata(DoorUri.parse(tempFile.toURI().toString()), ProcessContext(mutableMapOf<String,String>()))
        }

        Assert.assertEquals("Got expected title",
                "Ustad Mobile", contentEntry?.title)
        Assert.assertEquals("Got expected description",
            "Ustad Mobile sample tincan", contentEntry?.description)
    }


}