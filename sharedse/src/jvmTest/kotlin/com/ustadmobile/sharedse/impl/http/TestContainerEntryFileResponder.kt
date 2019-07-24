package com.ustadmobile.sharedse.impl.http

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryFile.Companion.COMPRESSION_GZIP
import com.ustadmobile.port.sharedse.impl.http.ContainerEntryFileResponder
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.zip.GZIPInputStream

class TestContainerEntryFileResponder {

    private val container: Container? = null

    private val containerManager: ContainerManager? = null

    private var appDatabase: UmAppDatabase? = null

    private var appRepo: UmAppDatabase? = null

    private fun getGZIPInputStreamFromResponse(data: InputStream, gzipHeader: Int): InputStream {
        return if (gzipHeader == COMPRESSION_GZIP) {
            GZIPInputStream(data)
        } else {
            data
        }
    }

    @Before
    @Throws(IOException::class)
    fun setup() {
        appDatabase = UmAppDatabase.getInstance(Any())
        appDatabase!!.clearAllTables()
        appRepo = appDatabase//appDatabase!!.getRepository("http://localhost/dummy/", "")
    }

    @Test
    @Throws(IOException::class)
    fun givenExistingContainerEntryFileUid_whenGetCalled_shouldReturnFileContents() {
        runBlocking {

            val container = Container()
            container.containerUid = appDatabase!!.containerDao.insert(container)
            val containerFileTmpDir = UmFileUtilSe.makeTempDir("testcontainerentryfileresponder",
                    "containerdir")
            val containerManager = ContainerManager(container, appDatabase!!, appRepo!!,
                    containerFileTmpDir.absolutePath)
            val fileToAdd = File.createTempFile("testcontainerentryfileresponder", "tmpfile")
            UmFileUtilSe.extractResourceToFile("/com/ustadmobile/port/sharedse/container/",
                    fileToAdd)
            containerManager.addEntries(ContainerManager.FileEntrySource(fileToAdd, "testfile1.png"))

            val mockSession = mock(NanoHTTPD.IHTTPSession::class.java)
            `when`(mockSession.uri).thenReturn("/ContainerEntryFile/" + containerManager.allEntries[0].ceCefUid)

            val mockUriResource = mock(RouterNanoHTTPD.UriResource::class.java)
            `when`(mockUriResource.initParameter(0, UmAppDatabase::class.java)).thenReturn(appDatabase)

            val response = ContainerEntryFileResponder().get(mockUriResource, mutableMapOf(),
                    mockSession)

            val entry = containerManager.allEntries[0]
            val containerIn = containerManager.getInputStream(entry)
            Assert.assertTrue("Response contents equals file contents",
                    Arrays.equals(UMIOUtils.readStreamToByteArray(containerIn),
                            UMIOUtils.readStreamToByteArray(getGZIPInputStreamFromResponse(response.data, entry.containerEntryFile!!.compression))))
            Assert.assertEquals("Response status is 200 OK", NanoHTTPD.Response.Status.OK,
                    response.status)
        }
    }

    @Test
    fun givenNonExistingFileUid_whenGetCalled_shouldReturn404() {
        val mockSession = mock(NanoHTTPD.IHTTPSession::class.java)
        `when`(mockSession.uri).thenReturn("/ContainerEntryFile/-1")

        val mockUriResource = mock(RouterNanoHTTPD.UriResource::class.java)
        `when`(mockUriResource.initParameter(0, UmAppDatabase::class.java)).thenReturn(appDatabase)

        val response = ContainerEntryFileResponder().get(mockUriResource, mutableMapOf(),
                mockSession)

        Assert.assertEquals("Response status is 404 not found when file does not exist",
                NanoHTTPD.Response.Status.NOT_FOUND, response.status)
    }

    companion object {

        private val RES_FOLDER = "/com/ustadmobile/port/sharedse/container/"

        private val RES_FILENAMES = arrayOf("testfile1.png", "testfile2.png")
    }


}
