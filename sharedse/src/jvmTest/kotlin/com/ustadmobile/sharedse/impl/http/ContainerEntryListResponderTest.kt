package com.ustadmobile.sharedse.impl.http

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.mockito.kotlin.mock
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryWithMd5
import com.ustadmobile.port.sharedse.impl.http.ContainerEntryListResponder
import com.ustadmobile.port.sharedse.impl.http.ContainerEntryListResponder.Companion.PARAM_CONTAINER_UID
import com.ustadmobile.sharedse.util.UstadTestRule
import com.ustadmobile.sharedse.util.activeDbInstance
import com.ustadmobile.sharedse.util.activeRepoInstance
import com.ustadmobile.test.util.ext.insertContainerFromResources
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import java.io.IOException

class ContainerEntryListResponderTest {

    private lateinit var container: Container

    private lateinit var di: DI

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    @JvmField
    @Rule
    var testFileRule = TemporaryFolder()

    @Before
    @Throws(IOException::class)
    fun setupDb() {
        di = DI {
            import(ustadTestRule.diModule)
        }

        val activeDb: UmAppDatabase by di.activeDbInstance()
        val activeRepo: UmAppDatabase by di.activeRepoInstance()
        container = runBlocking {
            insertContainerFromResources(activeDb, activeRepo, testFileRule.newFolder(),
                    testFileRule.newFolder(), di,
                    *RES_FILENAMES.map { "$RES_FOLDER$it" }.toTypedArray())
        }
    }


    @Test
    @Throws(IOException::class)
    fun givenContainerWithFiles_whenGetRequestedMade_thenShouldReturnFileList() {
        val responder = ContainerEntryListResponder()

        val mockUriResource = mock<RouterNanoHTTPD.UriResource> {
            on { initParameter(0, DI::class.java) }.thenReturn(di)
        }

        val mockSession = mock<NanoHTTPD.IHTTPSession> {
            on { parameters }.thenReturn(mutableMapOf(PARAM_CONTAINER_UID to listOf(container.containerUid.toString())))
        }

        val accountManager: UstadAccountManager by di.instance()
        val response = responder.get(mockUriResource,
                mutableMapOf("endpoint" to accountManager.activeAccount.endpointUrl), mockSession)

        Assert.assertNotNull("Response is not null", response)
        val responseStr = String(response.data.readBytes())
        val containerEntryList = Gson().fromJson<List<ContainerEntryWithMd5>>(responseStr,
                object : TypeToken<List<ContainerEntryWithMd5>>() {

                }.type)
        Assert.assertEquals("List has two entries", 2, containerEntryList.size.toLong())
    }

    @Test
    fun givenContainerUidWithNoFiles_whenGetRequestMade_thenShouldReturn404NotFound() {
        val responder = ContainerEntryListResponder()

        val mockSession = mock<NanoHTTPD.IHTTPSession> {
            on { parameters }.thenReturn(mapOf(PARAM_CONTAINER_UID to listOf("0")))
        }

        val mockUriResource = mock<RouterNanoHTTPD.UriResource> {
            on {initParameter(0, DI::class.java)}.thenReturn(di)
        }

        val accountManager: UstadAccountManager by di.instance()
        val response = responder.get(mockUriResource,
                mutableMapOf("endpoint" to accountManager.activeAccount.endpointUrl), mockSession)
        Assert.assertEquals("When making a request for a container that has no entries, 404 status " + "is returns", NanoHTTPD.Response.Status.NOT_FOUND, response.getStatus())

    }

    companion object {

        private const val RES_FOLDER = "/com/ustadmobile/port/sharedse/container/"

        private val RES_FILENAMES = arrayOf("testfile1.png", "testfile2.png")
    }


}
