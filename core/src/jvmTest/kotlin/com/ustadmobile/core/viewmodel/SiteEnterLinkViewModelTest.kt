package com.ustadmobile.core.viewmodel

import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.view.Login2View
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.Site
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okio.Buffer
import org.junit.Assert
import org.junit.Test
import org.kodein.di.*
import org.mockito.kotlin.*

class SiteEnterLinkViewModelTest {

    @Test
    fun givenValidLinkEntered_whenOnClickNextCalled_thenShouldNavigateToNextScreen() = testViewModel<SiteEnterLinkViewModel>(
        makeViewModel = { SiteEnterLinkViewModel(di, savedStateHandle) }
    ) {
        mockWebServer.start()
        val json: Json = di.direct.instance()

        val workSpace = json.encodeToString(Site().apply {
            siteName = "Dummy site"
            registrationAllowed = true
            guestLogin = true
        })

        mockWebServer.enqueue(
            MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBody(Buffer().write(workSpace.toByteArray())))

        val workSpacelink = "${mockWebServer.url("/")}"

        viewModel.onSiteLinkUpdated(workSpacelink)
        viewModel.onClickNext()

        runBlocking {
            withTimeout(5000) {
                Assert.assertNotNull("When receiving a valid link viewmodel emits the link and sets error state to null",
                    viewModel.uiState.firstOrNull {
                        it.siteLink == workSpacelink && it.linkError == null
                    })
                verify(navController, timeout(5000)).navigate(eq(Login2View.VIEW_NAME),
                    argWhere {
                        it[UstadView.ARG_SERVER_URL] == workSpacelink
                    }, any())
            }
        }
    }

    @Test
    fun givenInvalidLinkEntered_whenOnClickNextCalled_thenShouldShowError() = testViewModel<SiteEnterLinkViewModel>(
        makeViewModel = { SiteEnterLinkViewModel(di, savedStateHandle) }
    ) {
        val siteLink = "invalid"

        viewModel.onSiteLinkUpdated("invalid")
        viewModel.onClickNext()
        runBlocking {
            withTimeout(5000) {
                Assert.assertNotNull("Ui State is updated to show an error",
                    viewModel.uiState.firstOrNull {
                        it.siteLink == siteLink && it.linkError != null
                    }
                )
            }
        }
    }

}