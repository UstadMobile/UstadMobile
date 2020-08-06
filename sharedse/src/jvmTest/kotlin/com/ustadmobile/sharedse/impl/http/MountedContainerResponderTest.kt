package com.ustadmobile.sharedse.impl.http

import com.nhaarman.mockitokotlin2.mock
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.ContainerManagerCommon
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.port.sharedse.impl.http.MountedContainerResponder
import com.ustadmobile.port.sharedse.impl.http.MountedContainerResponder.Companion.PARAM_CONTAINERUID_INDEX
import com.ustadmobile.port.sharedse.impl.http.MountedContainerResponder.Companion.PARAM_DB_INDEX
import com.ustadmobile.port.sharedse.impl.http.MountedContainerResponder.Companion.PARAM_FILTERS_INDEX
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.zip.GZIPInputStream

class MountedContainerResponderTest {

    private lateinit var containerTmpDir: File

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var container: Container

    private var containerManager: ContainerManager? = null

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    private fun NanoHTTPD.Response.dataInflatedIfRequired(): InputStream{
        val gzipHeader = getHeader("Content-Encoding")
        return if(gzipHeader == "gzip") {
            GZIPInputStream(data)
        }else {
            data
        }
    }

    @Before
    @Throws(IOException::class)
    fun setup() {
        containerTmpDir = temporaryFolder.newFolder("TestMountedContainerResponder-containerTmp")

        db = UmAppDatabase.getInstance(Any())
        repo = db
        db.clearAllTables()

        container = Container()
        container.containerUid = repo.containerDao.insert(container)
        containerManager = ContainerManager(container!!, db!!, repo!!,
                containerTmpDir.absolutePath)
        val tmpExtractFile = File(containerTmpDir, "testfile1.png")
        UmFileUtilSe.extractResourceToFile("/com/ustadmobile/port/sharedse/container/testfile1.png",
                tmpExtractFile)
        runBlocking {
            containerManager!!.addEntries(ContainerManagerCommon.AddEntryOptions(dontUpdateTotals = false), ContainerManager.FileEntrySource(tmpExtractFile, "subfolder/testfile1.png"))
        }
    }

    @Test
    @Throws(IOException::class)
    fun givenContainerMounted_whenGetCalledWithPathThatExists_thenFileContentsShouldMatch() {
        val responder = MountedContainerResponder()
        val mountPath = "container/${container.containerUid}/"

        val mockSession = mock<NanoHTTPD.IHTTPSession> {
            on {uri}.thenReturn("${mountPath}subfolder/testfile1.png")
        }

        val mockUriResource = mock<RouterNanoHTTPD.UriResource> {
            on { initParameter(PARAM_CONTAINERUID_INDEX, String::class.java) }.thenReturn(container.containerUid.toString())
            on { initParameter(PARAM_DB_INDEX, UmAppDatabase::class.java)}.thenReturn(db)
            on { initParameter(PARAM_FILTERS_INDEX, MutableList::class.java)}.thenReturn(mutableListOf<Any>())
            on { uri }.thenReturn(mountPath + MountedContainerResponder.URI_ROUTE_POSTFIX)
        }



        val response = responder.get(mockUriResource, mutableMapOf(), mockSession)
        val gzipHeader = response.getHeader("Content-Encoding")
        Assert.assertEquals("Content was gzipped", "gzip", gzipHeader)
        val containerIn = containerManager!!.getInputStream(
                containerManager!!.getEntry("subfolder/testfile1.png")!!)
        Assert.assertArrayEquals("Data returned by URI responder matches actual container entry",
                containerIn.use { it.readBytes() },
                response.dataInflatedIfRequired().use { it.readBytes() })
        containerIn.close()
        Assert.assertEquals("Response is 200 OK", NanoHTTPD.Response.Status.OK,
                response.status)
    }

    @Test
    fun givenContainerMountedWithNonExisting_whenGetCalledWithNonExistantPath_thenShouldReturn404() {
        val responder = MountedContainerResponder()

        val mountPath = "container/${container.containerUid}/"
        val mockSession = mock<NanoHTTPD.IHTTPSession> {
            on { uri }.thenReturn(mountPath + "subfolder/doesnotexist.png")
        }

        val mockUriResource = mock<RouterNanoHTTPD.UriResource> {
            on { initParameter(PARAM_CONTAINERUID_INDEX, String::class.java) }.thenReturn(container.containerUid.toString())
            on { initParameter(PARAM_DB_INDEX, UmAppDatabase::class.java)}.thenReturn(db)
            on { initParameter(PARAM_FILTERS_INDEX, MutableList::class.java)}.thenReturn(mutableListOf<Any>())
            on { uri }.thenReturn(mountPath + MountedContainerResponder.URI_ROUTE_POSTFIX)
        }

        val response = responder.get(mockUriResource, mutableMapOf(), mockSession)
        Assert.assertEquals("Response is 404", NanoHTTPD.Response.Status.NOT_FOUND,
                response.getStatus())
    }

}
