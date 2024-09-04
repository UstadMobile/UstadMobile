package com.ustadmobile.core.viewmodel.siteenterlink

import app.cash.turbine.test
import com.ustadmobile.core.impl.nav.NavigateNavCommand
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.viewmodel.login.LoginViewModel
import com.ustadmobile.lib.db.entities.Site
import kotlinx.coroutines.flow.filter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.MockResponse
import okio.Buffer
import org.junit.Test
import org.kodein.di.*
import kotlin.test.assertNotNull

@Suppress("RemoveExplicitTypeArguments")
class LearningSpaceEnterLinkViewModelTest : AbstractMainDispatcherTest(){

    @Test
    fun givenValidLinkEntered_whenOnClickNextCalled_thenShouldNavigateToNextScreen(

    ) = testViewModel<LearningSpaceEnterLinkViewModel>(

    ) {
        viewModelFactory {
            LearningSpaceEnterLinkViewModel(di, savedStateHandle)
        }

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

        viewModel.navCommandFlow.filter {
            (it as? NavigateNavCommand)?.viewName == LoginViewModel.DEST_NAME
        }.test(name = "Receive navigate to LoginView") {
            viewModel.onClickNext()
            assertNotNull(awaitItem())
        }

        viewModel.uiState.assertItemReceived(name = "ui state updated as expected") {
            it.siteLink == workSpacelink && it.linkError == null
        }
    }

    @Test
    fun givenInvalidLinkEntered_whenOnClickNextCalled_thenShouldShowError(

    ) = testViewModel<LearningSpaceEnterLinkViewModel> {
        val siteLink = "invalid"

        viewModelFactory {
            LearningSpaceEnterLinkViewModel(di, savedStateHandle)
        }

        viewModel.onSiteLinkUpdated("invalid")
        viewModel.onClickNext()

        viewModel.uiState.assertItemReceived(name = "Ui state updated to show error") {
            it.siteLink == siteLink && it.linkError != null
        }
    }

}