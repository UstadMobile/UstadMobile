package com.ustadmobile.sharedse.impl.http

import com.nhaarman.mockitokotlin2.mock
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_DB
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.util.UMURLEncoder
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.sharedse.impl.http.ConcatenatedContainerEntryFileResponder
import com.ustadmobile.port.sharedse.impl.http.ConcatenatedContainerEntryFileResponder.Companion.URI_PARAM_ENDPOINT
import com.ustadmobile.sharedse.io.ConcatenatedInputStream
import com.ustadmobile.sharedse.util.UstadTestRule
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
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream

class ConcatenatedContainerEntryFileResponderTest {

    @JvmField
    @Rule
    var testRule = UstadTestRule()

    @JvmField
    @Rule
    var tmpfileRule = TemporaryFolder()

    lateinit var di: DI

    lateinit var accountManager: UstadAccountManager

    lateinit var db: UmAppDatabase

    lateinit var container: Pair<Container, ContainerManager>

    @Before
    fun setup() {
        di = DI{
            import(testRule.diModule)
        }

        accountManager = di.direct.instance()
        db = di.on(accountManager.activeAccount).direct.instance(tag = TAG_DB)
        val repo: UmAppDatabase = di.on(accountManager.activeAccount).direct.instance(tag = TAG_REPO)

        container = runBlocking {
            insertContainerFromResources(db, repo, tmpfileRule.newFolder(), tmpfileRule.newFolder(),
                    *testResourcesList.toTypedArray())
        }
    }

    @Test
    fun givenValidRequest_whenGetCalled_thenShouldServeData() {
        val containerEntries = db.containerEntryDao.findByContainerWithMd5(container.first.containerUid)
        val containerEntryFileUids = containerEntries.map { it.ceCefUid }.joinToString(separator = ";") { it.toString() }
        val mockSession = mock<NanoHTTPD.IHTTPSession> {
            on { uri }.thenReturn("/${UMURLEncoder.encodeUTF8(accountManager.activeAccount.endpointUrl)}/$containerEntryFileUids")
        }

        val mockUriResource = mock<RouterNanoHTTPD.UriResource> {
            on { initParameter(0, DI::class.java )}.thenReturn(di)
        }

        val responder = ConcatenatedContainerEntryFileResponder()
        val response = responder.get(mockUriResource,
                mutableMapOf(URI_PARAM_ENDPOINT to accountManager.activeAccount.endpointUrl), mockSession)

        val concatenatedInputStream = ConcatenatedInputStream(response.data)
        containerEntries.forEachIndexed { index, containerEntry ->
            val resourcePath = testResourcesList.find { it.endsWith(containerEntry.cePath!!)}
            concatenatedInputStream.nextPart()
            val inBytes = GZIPInputStream(ByteArrayInputStream(concatenatedInputStream.readBytes())).readBytes()
            val resourceBytes = this::class.java.getResourceAsStream(resourcePath).use { it.readBytes() }
            Assert.assertArrayEquals("Resource $index bytes equal original",
                resourceBytes, inBytes)
        }
    }

    @Test
    fun givenValidRequest_whenHeadCalled_thenShouldServeHeadersWithNoData() {
        val containerEntries = db.containerEntryDao.findByContainerWithMd5(container.first.containerUid)
        val containerEntryFileUids = containerEntries.map { it.ceCefUid }.joinToString(separator = ";") { it.toString() }
        val mockSession = mock<NanoHTTPD.IHTTPSession> {
            on { uri }.thenReturn("/${UMURLEncoder.encodeUTF8(accountManager.activeAccount.endpointUrl)}/$containerEntryFileUids")
        }

        val mockUriResource = mock<RouterNanoHTTPD.UriResource> {
            on { initParameter(0, DI::class.java )}.thenReturn(di)
        }

        val headResponse = ConcatenatedContainerEntryFileResponder().other("HEAD",
            mockUriResource, mutableMapOf(URI_PARAM_ENDPOINT to accountManager.activeAccount.endpointUrl),
            mockSession)

        Assert.assertEquals("Head response has no data", 0, headResponse.data.readBytes().size)
        Assert.assertTrue("Head response has content length > 0",
                (headResponse.getHeader("content-length")?.toLong() ?: 0) > 0)
    }

    companion object {
        val testResourcesList = (1 .. 3).map { "/com/ustadmobile/core/container/testfile$it.png" }
    }

}