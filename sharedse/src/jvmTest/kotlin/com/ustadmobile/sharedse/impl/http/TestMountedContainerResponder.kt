package com.ustadmobile.sharedse.impl.http

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.ContainerManagerCommon
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.port.sharedse.impl.http.MountedContainerResponder
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
import java.util.zip.GZIPInputStream

class TestMountedContainerResponder {

    private var containerTmpDir: File? = null

    private var db: UmAppDatabase? = null

    private var repo: UmAppDatabase? = null

    private var container: Container? = null

    private var containerManager: ContainerManager? = null

    private var context = Any()

    private fun getGZIPInputStreamFromResponse(response: NanoHTTPD.Response): InputStream {
        val gzipHeader = response.getHeader("Content-Encoding")
        val data = response.data
        return if (gzipHeader != null && gzipHeader == "gzip") {
            GZIPInputStream(data)
        } else {
            data
        }
    }

    @Before
    @Throws(IOException::class)
    fun setup() {
        containerTmpDir = UmFileUtilSe.makeTempDir("TestMountedContainerResponder",
                "containerTmpDir")

        db = UmAppDatabase.getInstance(Any())
        repo = db //db!!.getRepository("http://localhost/dummy/", "")
        db!!.clearAllTables()

        container = Container()
        container!!.containerUid = repo!!.containerDao.insert(container!!)
        containerManager = ContainerManager(container!!, db!!, repo!!,
                containerTmpDir!!.absolutePath)
        val tmpExtractFile = File(containerTmpDir, "testfile1.png")
        UmFileUtilSe.extractResourceToFile("/com/ustadmobile/port/sharedse/container/testfile1.png",
                tmpExtractFile)
        runBlocking {
            containerManager!!.addEntries(ContainerManagerCommon.AddEntryOptions(dontUpdateTotals = false), ContainerManager.FileEntrySource(tmpExtractFile, "subfolder/testfile1.png"))
        }
    }

    @Throws(IOException::class)
    fun tearDown() {
        UmFileUtilSe.deleteRecursively(containerTmpDir!!)
    }

    @Test
    @Throws(IOException::class)
    fun givenContainerMounted_whenGetCalledWithPathThatExists_thenFileContentsShouldMatch() {
        val responder = MountedContainerResponder()

        val mockSession = mock(NanoHTTPD.IHTTPSession::class.java)
        val mountPath = "container/" + container!!.containerUid + "/"
        `when`(mockSession.uri).thenReturn(mountPath + "subfolder/testfile1.png")

        val mockUriResource = mock(RouterNanoHTTPD.UriResource::class.java)
        `when`(mockUriResource.initParameter(0, Any::class.java))
                .thenReturn(context)
        `when`(mockUriResource.initParameter(1, MutableList::class.java)).thenReturn(mutableListOf<Any>())
        `when`(mockUriResource.uri).thenReturn(mountPath + MountedContainerResponder.URI_ROUTE_POSTFIX)

        val response = responder.get(mockUriResource, mutableMapOf(), mockSession)
        val gzipHeader = response.getHeader("Content-Encoding")
        Assert.assertEquals("Content was gzipped", "gzip", gzipHeader)
        val containerIn = containerManager!!.getInputStream(
                containerManager!!.getEntry("subfolder/testfile1.png")!!)
        Assert.assertArrayEquals("Data returned by URI responder matches actual container entry",
                UMIOUtils.readStreamToByteArray(containerIn),
                UMIOUtils.readStreamToByteArray(getGZIPInputStreamFromResponse(response)))
        containerIn.close()
        Assert.assertEquals("Response is 200 OK", NanoHTTPD.Response.Status.OK,
                response.status)

    }

    @Test
    fun givenContainerMountedWithNonExisting_whenGetCalledWithNonExistantPath_thenShouldReturn404() {
        val responder = MountedContainerResponder()

        val mockSession = mock(NanoHTTPD.IHTTPSession::class.java)
        val mountPath = "container/" + container!!.containerUid + "/"
        `when`(mockSession.uri).thenReturn(mountPath + "subfolder/doesnotexist.png")

        val mockUriResource = mock(RouterNanoHTTPD.UriResource::class.java)
        `when`(mockUriResource.initParameter(0, Any::class.java))
                .thenReturn(context)
        `when`(mockUriResource.initParameter(1, MutableList::class.java)).thenReturn(mutableListOf<Any>())
        `when`(mockUriResource.uri).thenReturn(mountPath + MountedContainerResponder.URI_ROUTE_POSTFIX)

        val response = responder.get(mockUriResource, mutableMapOf(), mockSession)
        Assert.assertEquals("Response is 404", NanoHTTPD.Response.Status.NOT_FOUND,
                response.getStatus())
    }

}
