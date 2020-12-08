package com.ustadmobile.sharedse.impl.http

import com.nhaarman.mockitokotlin2.mock
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.container.ContainerManagerCommon
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.door.asRepository
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.sharedse.ext.dataInflatedIfRequired
import com.ustadmobile.port.sharedse.util.UmFileUtilSe
import com.ustadmobile.port.sharedse.impl.http.MountedContainerResponder
import com.ustadmobile.port.sharedse.impl.http.MountedContainerResponder.Companion.PARAM_CONTAINERUID_INDEX
import com.ustadmobile.port.sharedse.impl.http.MountedContainerResponder.Companion.PARAM_DB_INDEX
import com.ustadmobile.port.sharedse.impl.http.MountedContainerResponder.Companion.PARAM_FILTERS_INDEX
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpStatement
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

    private lateinit var containerManager: ContainerManager

    @JvmField
    @Rule
    var temporaryFolder = TemporaryFolder()

    @Before
    @Throws(IOException::class)
    fun setup() {
        containerTmpDir = temporaryFolder.newFolder("TestMountedContainerResponder-containerTmp")

        db = UmAppDatabase.getInstance(Any())
        repo = db.asRepository(Any(), "http://localhost/dummy", "",
                defaultHttpClient())
        db.clearAllTables()

        container = Container()
        container.containerUid = repo.containerDao.insert(container)
        containerManager = ContainerManager(container, db, repo,
                containerTmpDir.absolutePath)

        val tmpFiles = (1..2).map {index ->
            File(containerTmpDir, "testfile$index.png").also {file ->
                UmFileUtilSe.extractResourceToFile("/com/ustadmobile/port/sharedse/container/testfile$index.png",
                        file)
            }
        }

        runBlocking {
            containerManager.addEntries(ContainerManagerCommon.AddEntryOptions(dontUpdateTotals = false),
                    ContainerManager.FileEntrySource(tmpFiles[0], "subfolder/testfile1.png"),
                    ContainerManager.FileEntrySource(tmpFiles[1], "subfolder/test file2.png"))
        }
    }

    //Test handling of file names when url encoding is required
    @Test
    fun givenFileNameWithSpaces_whenGetCalled_thenContentsShouldMatch() {
        val routerHttpd = RouterNanoHTTPD(0)
        routerHttpd.start()
        routerHttpd.addRoute("/endpoint/container/(.*)+", MountedContainerResponder::class.java,
                container.containerUid.toString(), db, listOf<MountedContainerResponder.MountedContainerFilter>())

        println("port = ${routerHttpd.listeningPort}")
        val httpClient = HttpClient()

        runBlocking {
            httpClient.get<HttpStatement>("http://localhost:${routerHttpd.listeningPort}/endpoint/container/subfolder/test%20file2.png").execute {
                Assert.assertEquals("Content status reported as 200", 200, it.status.value)
            }
        }

        httpClient.close()
        routerHttpd.stop()
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
        val containerIn = containerManager.getInputStream(
                containerManager.getEntry("subfolder/testfile1.png")!!)
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

    @Test
    fun givenContainerMountedWithExistingPath_whenGetCalledNotAcceptingGzip_thenShouldInflateContents() {
        val responder = MountedContainerResponder()
        val mountPath = "container/${container.containerUid}/"

        val mockSession = mock<NanoHTTPD.IHTTPSession> {
            on {uri}.thenReturn("${mountPath}subfolder/testfile1.png")
            on { headers }.thenReturn(mapOf("accept-encoding" to "identity"))
        }

        val mockUriResource = mock<RouterNanoHTTPD.UriResource> {
            on { initParameter(PARAM_CONTAINERUID_INDEX, String::class.java) }.thenReturn(container.containerUid.toString())
            on { initParameter(PARAM_DB_INDEX, UmAppDatabase::class.java)}.thenReturn(db)
            on { initParameter(PARAM_FILTERS_INDEX, MutableList::class.java)}.thenReturn(mutableListOf<Any>())
            on { uri }.thenReturn(mountPath + MountedContainerResponder.URI_ROUTE_POSTFIX)
        }

        val response = responder.get(mockUriResource, mutableMapOf(), mockSession)

        val containerIn = containerManager.getInputStream(
                containerManager.getEntry("subfolder/testfile1.png")!!)
        Assert.assertArrayEquals("Data returned by URI responder matches actual container entry",
                containerIn.use { it.readBytes() },
                response.data.use { it.readBytes() })
    }




}
