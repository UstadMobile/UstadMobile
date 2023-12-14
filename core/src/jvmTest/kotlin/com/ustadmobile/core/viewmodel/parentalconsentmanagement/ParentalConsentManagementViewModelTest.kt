package com.ustadmobile.core.viewmodel.parentalconsentmanagement

import app.cash.turbine.test
import com.ustadmobile.core.test.viewmodeltest.ViewModelTestBuilder
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.viewmodel.UstadViewModel
import com.ustadmobile.door.flow.doorFlow
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.ext.shallowCopy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

@Suppress("RemoveExplicitTypeArguments")
class ParentalConsentManagementViewModelTest : AbstractMainDispatcherTest(){

    data class ParentalConsentTestContext(
        val personParentJoin: PersonParentJoin,
        val parentPerson: Person,
        val minorPerson: Person,
    )

    private fun testParentalConsentManagementViewModel(
        block: suspend ViewModelTestBuilder<ParentalConsentManagementViewModel>.(ParentalConsentTestContext) -> Unit
    ) {
        testViewModel<ParentalConsentManagementViewModel> {
            val parentPerson = setActiveUser(activeEndpoint, Person().apply {
                firstNames = "Pit"
                lastName = "The Older"
            })


            val minorPerson = activeDb.insertPersonAndGroup(
                Person().apply {
                    firstNames = "Pit"
                    lastName = "The Young"
                    username = "pityoung"
                    dateOfBirth = Clock.System.now().minus(5, DateTimeUnit.YEAR, TimeZone.UTC)
                        .toEpochMilliseconds()
                }
            )

            val personParentJoin = PersonParentJoin().apply {
                ppjMinorPersonUid = minorPerson.personUid
                ppjUid = activeDb.personParentJoinDao.insertAsync(this)
            }

            val testContext = ParentalConsentTestContext(
                personParentJoin = personParentJoin,
                parentPerson = parentPerson,
                minorPerson = minorPerson
            )

            block(testContext)
        }
    }


    @Test
    fun givenPersonParentJoinHasNoParentYet_whenOpened_thenShouldSetParentAndApprovalStatus() {
        testParentalConsentManagementViewModel { context ->
            viewModelFactory {
                savedStateHandle[UstadViewModel.ARG_ENTITY_UID] = context.personParentJoin.ppjUid.toString()
                ParentalConsentManagementViewModel(di, savedStateHandle)
            }

            val readyUiState = viewModel.uiState.first {
                it.fieldsEnabled
            }

            viewModel.onEntityChanged(
                readyUiState.parentJoinAndMinor?.personParentJoin?.shallowCopy {
                    ppjStatus = PersonParentJoin.STATUS_APPROVED
                    ppjRelationship = PersonParentJoin.RELATIONSHIP_MOTHER
                }
            )

            viewModel.onClickConsent()
            val joinFlow = activeDb.doorFlow(
                arrayOf("PersonParentJoin"),
            ) {
                activeDb.personParentJoinDao.findByUidWithMinorAsync(context.personParentJoin.ppjUid)
            }

            joinFlow.filter {
                it?.personParentJoin?.ppjStatus == PersonParentJoin.STATUS_APPROVED
            }.test(timeout = 500.seconds) {
                val approvedStatus = awaitItem()
                assertEquals(PersonParentJoin.STATUS_APPROVED,
                    approvedStatus?.personParentJoin?.ppjStatus)
                assertEquals(context.parentPerson.personUid,
                    approvedStatus?.personParentJoin?.ppjParentPersonUid)
                assertEquals(PersonParentJoin.RELATIONSHIP_MOTHER,
                    approvedStatus?.personParentJoin?.ppjRelationship)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

}