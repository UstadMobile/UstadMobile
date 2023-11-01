package com.ustadmobile.core.viewmodel.epubcontent

import app.cash.turbine.test
import com.ustadmobile.core.contentformats.epub.opf.Package
import com.ustadmobile.core.io.ext.readString
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.url.UrlKmp
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.lib.db.entities.ContentEntryVersion
import kotlinx.coroutines.flow.filter
import nl.adaptivity.xmlutil.serialization.XML
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.kodein.di.direct
import org.kodein.di.instance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class EpubContentViewModelTest : AbstractMainDispatcherTest(){

    @Test
    fun givenValidEpub_whenInitialized_thenShouldSetSpineAndTitle() {
        val opfText = this::class.java.getResourceAsStream(
            "/com/ustadmobile/core/contentformats/epub/opf/TestOpfDocument-valid.opf"
        )!!.readString()

        testViewModel<EpubContentViewModel> {
            mockWebServer.dispatcher = object: Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    return MockResponse()
                        .addHeader("content-type", "application/oebps-package+xml")
                        .setBody(opfText)
                }
            }
            val xml: XML = di.direct.instance()

            val cevUid = activeDb.doorPrimaryKeyManager.nextId(ContentEntryVersion.TABLE_ID)
            val contentEntryVersion = ContentEntryVersion(
                cevUid = cevUid,
                cevUrl = mockWebServer.url("/$cevUid/book.opf").toString()
            )
            activeDb.contentEntryVersionDao.insertAsync(contentEntryVersion)

            viewModelFactory {
                savedStateHandle[UstadViewModel.ARG_ENTITY_UID] = cevUid.toString()
                EpubContentViewModel(di, savedStateHandle)
            }

            viewModel.uiState.filter {
                it.spineUrls.isNotEmpty()
            }.test(timeout = 5.seconds, name = "Spine urls will be set") {
                val uiState = awaitItem()
                assertEquals(7, uiState.spineUrls.size)
                val opfPackage = xml.decodeFromString(Package.serializer(), opfText)
                val opfUrl = UrlKmp(contentEntryVersion.cevUrl!!)
                opfPackage.spine.itemRefs.forEachIndexed { index, itemRef ->
                    val item = opfPackage.manifest.items.first { it.id == itemRef.idRef }
                    assertEquals(opfUrl.resolve(item.href).toString(), uiState.spineUrls[index])
                }

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

}