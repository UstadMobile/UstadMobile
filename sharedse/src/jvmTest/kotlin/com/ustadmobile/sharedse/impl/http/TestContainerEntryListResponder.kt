package com.ustadmobile.sharedse.impl.http

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.UMIOUtils
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import com.ustadmobile.port.sharedse.impl.http.ContainerEntryListResponder
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
import java.util.*

class TestContainerEntryListResponder {

    private var container: Container? = null

    private var containerManager: ContainerManager? = null

    private var appDatabase: UmAppDatabase? = null

    private var appRepo: UmAppDatabase? = null

    @Before
    @Throws(IOException::class)
    fun setupDb() {
        appDatabase = UmAppDatabase.getInstance(Any())
        appDatabase!!.clearAllTables()
        appRepo = appDatabase//appDatabase!!.getRepository("http://localhost/dummy/", "")

        val tmpDir = File.createTempFile("testresponder", "tmpdir")
        tmpDir.delete()
        tmpDir.mkdirs()

        val containerTmpDir = File.createTempFile("testresponder", "containerfiles")
        containerTmpDir.delete()
        containerTmpDir.mkdirs()

        val fileMap = HashMap<File, String>()
        for (filename in RES_FILENAMES) {
            val resFile = File(tmpDir, filename)
            UmFileUtilSe.extractResourceToFile(RES_FOLDER + filename,
                    resFile)
            fileMap[resFile] = filename
        }

        container = Container()
        container!!.containerUid = appDatabase!!.containerDao.insert(container!!)

        runBlocking {
            containerManager = ContainerManager(container!!, appDatabase!!, appRepo!!,
                    containerTmpDir.absolutePath)
            for(fileName in RES_FILENAMES){
                val resFile = File(tmpDir, fileName)
                UmFileUtilSe.extractResourceToFile(RES_FOLDER + fileName,
                        resFile)
                containerManager!!.addEntries(ContainerManager.FileEntrySource(resFile, fileName))
            }

        }
    }

    @ExperimentalStdlibApi
    @Test
    @Throws(IOException::class)
    fun givenContainerWithFiles_whenGetRequestedMade_thenShouldReturnFileList() {
        val responder = ContainerEntryListResponder()

        val mockSession = mock(NanoHTTPD.IHTTPSession::class.java)

        val mockParamMap = HashMap<String, List<String>>()
        mockParamMap[ContainerEntryListResponder.PARAM_CONTAINER_UID] = Arrays.asList(container!!.containerUid.toString())
        `when`(mockSession.parameters).thenReturn(mockParamMap)

        val mockUriResource = mock(RouterNanoHTTPD.UriResource::class.java)
        `when`(mockUriResource.initParameter(0, UmAppDatabase::class.java)).thenReturn(appDatabase)


        val response = responder.get(mockUriResource, mutableMapOf(), mockSession)


        Assert.assertNotNull("Response is not null", response)
        val responseStr = UMIOUtils.readStreamToString(response.data)
        val containerEntryList = Gson().fromJson<List<ContainerEntryWithMd5>>(responseStr,
                object : TypeToken<List<ContainerEntryWithMd5>>() {

                }.type)
        Assert.assertEquals("List has two entries", 2, containerEntryList.size.toLong())
    }

    @Test
    fun givenContainerUidWithNoFiles_whenGetRequestMade_thenShouldReturn404NotFound() {
        val responder = ContainerEntryListResponder()

        val mockSession = mock(NanoHTTPD.IHTTPSession::class.java)

        val mockParamMap = HashMap<String, List<String>>()
        mockParamMap[ContainerEntryListResponder.PARAM_CONTAINER_UID] = Arrays.asList(0.toString())
        `when`(mockSession.parameters).thenReturn(mockParamMap)

        val mockUriResource = mock(RouterNanoHTTPD.UriResource::class.java)
        `when`(mockUriResource.initParameter(0, UmAppDatabase::class.java)).thenReturn(appDatabase)

        val response = responder.get(mockUriResource, mutableMapOf(), mockSession)
        Assert.assertEquals("When making a request for a container that has no entries, 404 status " + "is returns", NanoHTTPD.Response.Status.NOT_FOUND, response.getStatus())

    }

    companion object {

        private const val RES_FOLDER = "/com/ustadmobile/port/sharedse/container/"

        private val RES_FILENAMES = arrayOf("testfile1.png", "testfile2.png")
    }


}
