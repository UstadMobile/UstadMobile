package com.ustadmobile.core.viewmodel.person.detail

import app.cash.turbine.test
import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.test.viewmodeltest.assertItemReceived
import com.ustadmobile.core.test.viewmodeltest.testViewModel
import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.test.AbstractMainDispatcherTest
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.ScopedGrant
import kotlinx.coroutines.delay
import org.junit.Test
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import kotlin.time.Duration.Companion.minutes
import kotlin.test.assertFalse

class PersonDetailViewModelTest: AbstractMainDispatcherTest() {

    val endpoint = Endpoint("http://test.com/")

    @Test
    fun givenPersonDetails_whenPersonUsernameIsNullAndCantManageAccount_thenCreateAccountShouldBeHidden() {
        testViewModel<PersonDetailViewModel>() {
            setActiveUser(endpoint)
            val db: UmAppDatabase = di.direct.on(endpoint).instance(tag = DoorTag.TAG_DB)

            val personBeingViewed = db.insertPersonAndGroup(Person().apply {
                firstNames = "Lenny"
                lastName = "Fluff"
                username = null
            })

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = personBeingViewed.personUid.toString()
                PersonDetailViewModel(di, savedStateHandle)
            }

            viewModel.uiState.assertItemReceived(timeout = 2.minutes) {
                it.person?.firstNames == "Lenny" && !it.changePasswordVisible && !it.showCreateAccountVisible
            }
        }
    }

    @Test
    fun givenPersonDetailsAndAdminLogged_whenPersonUsernameIsNullAndCanManageAccount_thenCreateAccountShouldBeShown() {
        testViewModel<PersonDetailViewModel> {
            val activeUser = setActiveUser(endpoint)
            val db: UmAppDatabase = di.direct.on(endpoint).instance(tag = DoorTag.TAG_DB)

            db.grantScopedPermission(
                activeUser, Role.ALL_PERMISSIONS, ScopedGrant.ALL_TABLES,
                ScopedGrant.ALL_ENTITIES
            )

            val personBeingViewed = db.insertPersonAndGroup(Person().apply {
                firstNames = "Lenny"
                lastName = "Fluff"
                username = null
            })

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = personBeingViewed.personUid.toString()
                PersonDetailViewModel(di, savedStateHandle)
            }

            viewModel.uiState.assertItemReceived {
                it.person?.firstNames == "Lenny" && !it.changePasswordVisible && it.showCreateAccountVisible
            }
        }
    }

    @Test
    fun givenPersonDetailsAndAdminLogged_whenPersonUsernameIsNotNullAndCanManageAccount_thenChangePasswordShouldBeShown() {
        testViewModel<PersonDetailViewModel> {
            val activeUser = setActiveUser(endpoint)
            val db: UmAppDatabase = di.direct.on(endpoint).instance(tag = DoorTag.TAG_DB)

            db.grantScopedPermission(
                activeUser, Role.ALL_PERMISSIONS, ScopedGrant.ALL_TABLES,
                ScopedGrant.ALL_ENTITIES
            )

            val personBeingViewed = db.insertPersonAndGroup(Person().apply {
                firstNames = "Lenny"
                lastName = "Fluff"
                username = "lenny"
            })

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = personBeingViewed.personUid.toString()
                PersonDetailViewModel(di, savedStateHandle)
            }

            viewModel.uiState.assertItemReceived {
                it.person?.firstNames == "Lenny" && it.changePasswordVisible && !it.showCreateAccountVisible
            }
        }
    }

    @Test
    fun givenPersonDetails_whenOpenedActivePersonDetailPersonAndCanManageAccount_thenChangePasswordShouldBeShown() {
        testViewModel<PersonDetailViewModel> {
            val activeUser = setActiveUser(endpoint)

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = activeUser.personUid.toString()
                PersonDetailViewModel(di, savedStateHandle)
            }

            viewModel.uiState.assertItemReceived {
                it.changePasswordVisible
            }
        }
    }


    @Test
    fun givenActiveUserIsParent_whenOpenChildProfile_thenShouldShowManageParentalConsent() {
        testViewModel<PersonDetailViewModel> {
            val activeUser = setActiveUser(endpoint)
            val db: UmAppDatabase = di.direct.on(endpoint).instance(tag = DoorTag.TAG_DB)

            val child = db.withDoorTransactionAsync {
                val childInDb = db.insertPersonAndGroup(Person().apply {
                    firstNames = "Bob"
                    lastName = "Young"
                    dateOfBirth = systemTimeInMillis() - (10 * 365 * 24 * 60 * 60 * 1000L)
                    username = "young"
                })

                db.personParentJoinDao.insertAsync(PersonParentJoin().apply {
                    ppjMinorPersonUid = childInDb.personUid
                    ppjParentPersonUid = activeUser.personUid
                    ppjRelationship = PersonParentJoin.RELATIONSHIP_MOTHER
                })

                childInDb
            }

            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = child.personUid.toString()
                PersonDetailViewModel(di, savedStateHandle)
            }

            viewModel.uiState.assertItemReceived {
                it.manageParentalConsentVisible
            }
        }
    }

    @Test
    fun givenActiveUserIsNotParent_whenOpenChildProfile_thenShouldShowManageParentalConsent() {
        testViewModel<PersonDetailViewModel> {
            val activeUser = setActiveUser(endpoint)
            val db: UmAppDatabase = di.direct.on(endpoint).instance(tag = DoorTag.TAG_DB)

            db.withDoorTransactionAsync {
                db.insertPersonAndGroup(Person().apply {
                    firstNames = "Bob"
                    lastName = "Young"
                    dateOfBirth = systemTimeInMillis() - (10 * 365 * 24 * 60 * 60 * 1000L)
                    username = "young"
                })
            }


            viewModelFactory {
                savedStateHandle[UstadView.ARG_ENTITY_UID] = activeUser.personUid.toString()
                PersonDetailViewModel(di, savedStateHandle)
            }

            viewModel.uiState.test {
                delay(250)
                assertFalse(expectMostRecentItem().manageParentalConsentVisible,
                    "Manage parental consent is not visible")
            }
        }
    }

}