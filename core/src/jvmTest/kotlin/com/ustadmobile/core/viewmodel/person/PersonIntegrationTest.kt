package com.ustadmobile.core.viewmodel.person

import app.cash.turbine.test
import com.ustadmobile.core.test.clientservertest.clientServerIntegrationTest
import com.ustadmobile.core.test.savedStateOf
import com.ustadmobile.core.test.use
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.util.ext.awaitItemWhere
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.viewmodel.person.accountedit.PersonAccountEditViewModel
import com.ustadmobile.core.viewmodel.person.detail.PersonDetailViewModel
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.util.test.initNapierLog
import kotlinx.coroutines.flow.first
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class PersonIntegrationTest: AbstractMainDispatcherTest() {

    //@Test
    fun givenAdminCreatesAccount_whenPersonLogsIn_thenShouldSucceed() {
        initNapierLog()

        clientServerIntegrationTest(
            numClients = 2
        ) {
            val createdUser = serverDb.insertPersonAndGroup(
                Person().apply {
                    firstNames = "Bart"
                    lastName = "Simpson"
                }
            )

            val client = clients.first()
            client.login("admin", "admin")

            //Admin sets password
            PersonAccountEditViewModel(
                di = client.di,
                savedStateHandle = savedStateOf(
                    UstadView.ARG_ENTITY_UID to createdUser.personUid.toString()
                )
            ).use { viewModel ->
                viewModel.uiState.test(timeout = 5.seconds, name = "admin can set password") {
                    val readyState = awaitItemWhere {
                        it.usernameVisible && it.fieldsEnabled
                    }
                    viewModel.onEntityChanged(readyState.personAccount?.copy(
                        username = "bart",
                        newPassword = "simpson"
                    ))
                    viewModel.onClickSave()

                    //Wait for save to finish
                    viewModel.navCommandFlow.first()

                    cancelAndIgnoreRemainingEvents()
                }
            }

            PersonDetailViewModel(
                di = client.di,
                savedStateHandle = savedStateOf(
                    UstadView.ARG_ENTITY_UID to createdUser.personUid.toString()
                )
            ).use { viewModel ->
                viewModel.uiState.assertItemReceived(timeout = 5.seconds, name = "detail view model shows account created") {
                    println("Person detail state username = ${it.person?.person?.username} hasPasswordPermission=${it.hasChangePasswordPermission}")
                    it.person?.person?.username == "bart" && !it.showCreateAccountVisible && it.changePasswordVisible
                }
            }

        }
    }

}