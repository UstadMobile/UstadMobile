
package com.ustadmobile.core.controller


import com.ustadmobile.core.account.UstadAccountManager
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.ustadmobile.core.view.ParentalConsentManagementView
import org.mockito.kotlin.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.core.db.dao.PersonParentJoinDao
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.door.lifecycle.LifecycleObserver
import com.ustadmobile.lib.db.entities.PersonParentJoin


import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import org.junit.Assert
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeDbInstance
import com.ustadmobile.core.util.activeRepoInstance
import kotlinx.coroutines.runBlocking
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.core.util.mockLifecycleOwner
import com.ustadmobile.core.util.test.waitUntil
import com.ustadmobile.door.lifecycle.DoorState
import org.kodein.di.DI
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.util.test.ext.startLocalTestSessionBlocking
import org.kodein.di.instance


/**
 * The Presenter test for list items is generally intended to be a sanity check on the underlying code.
 *
 * Note:
 */
class ParentalConsentManagementPresenterTest {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var di: DI

    private lateinit var mockView: ParentalConsentManagementView

    private lateinit var context: Any

    private lateinit var mockLifecycleOwner: LifecycleOwner

    private lateinit var repoPersonParentJoinDaoSpy: PersonParentJoinDao

    private lateinit var personParentJoin: PersonParentJoin

    private lateinit var parentPerson: Person

    private lateinit var minorPerson: Person

    @Before
    fun setup() {
        mockView = mock { }
        mockLifecycleOwner = mockLifecycleOwner(DoorState.RESUMED)
        context = Any()

        di = DI {
            import(ustadTestRule.diModule)
        }

        val repo: UmAppDatabase by di.activeRepoInstance()

        repoPersonParentJoinDaoSpy = spy(repo.personParentJoinDao)
        whenever(repo.personParentJoinDao).thenReturn(repoPersonParentJoinDaoSpy)

        //TODO: insert any entities required for all tests

        parentPerson = runBlocking {
            repo.insertPersonAndGroup(Person().apply {
                firstNames = "Pit"
                lastName = "The Older"
                dateOfBirth = (systemTimeInMillis() - 30 * 365 * 24 * 60 * 60 * 1000L)
            })
        }

        val accountManager: UstadAccountManager by di.instance()

        val activeEndpoint: String = accountManager.activeEndpoint.url
        accountManager.startLocalTestSessionBlocking(parentPerson, activeEndpoint)

        minorPerson = Person().apply {
            firstNames = "Pit"
            lastName = "The Young"
            username = "pityoung"
            dateOfBirth = systemTimeInMillis() - (10 * 365 * 24 * 60 * 60 * 1000L)
            personUid = repo.personDao.insert(this)
        }

        personParentJoin = PersonParentJoin().apply {
            ppjMinorPersonUid = minorPerson.personUid
            ppjUid = runBlocking { repo.personParentJoinDao.insertAsync(this@apply) }
        }
    }

    @Test
    fun givenPersonParentJoinHasNoParentYet_whenOpened_thenShouldSetParentAndApprovalStatus() {
        val db: UmAppDatabase by di.activeDbInstance()
        val repo: UmAppDatabase by di.activeRepoInstance()
        val accountManager: UstadAccountManager by di.instance()

        val activeEndpoint: String = accountManager.activeEndpoint.url

        val presenterArgs = mapOf(ARG_ENTITY_UID to personParentJoin.ppjUid.toString())
        val presenter = ParentalConsentManagementPresenter(context,
                presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue(5000 * 1000)!!

        //Make some changes to the entity (e.g. as the user would do using data binding)
        //e.g. initialEntity!!.someName = "New Spelling Clazz"

        initialEntity?.ppjStatus = PersonParentJoin.STATUS_APPROVED
        initialEntity?.ppjRelationship = PersonParentJoin.RELATIONSHIP_MOTHER
        presenter.handleClickSave(initialEntity)

        runBlocking {
            db.waitUntil(5000, listOf("PersonParentJoin")) {
                runBlocking {
                    db.personParentJoinDao.findByUidWithMinorAsync(personParentJoin.ppjUid)?.ppjParentPersonUid != 0L
                }
            }
        }

        val entitySaved = runBlocking {
            db.personParentJoinDao.findByUidWithMinorAsync(personParentJoin.ppjUid)
        }

        Assert.assertEquals("Status was set to approved",
                PersonParentJoin.STATUS_APPROVED, entitySaved?.ppjStatus)
        Assert.assertEquals("Person name was displayed as expected",
            "Pit", initialEntity.minorPerson?.firstNames)
        Assert.assertEquals("Parent uid was set on saved entity",
            parentPerson.personUid, entitySaved?.ppjParentPersonUid)

    }

    @Test
    fun givenParentChildJoinNoRelationshipSelected_whenClickSaveCalled_thenShouldSetErrorMessage() {
        val presenterArgs = mapOf(ARG_ENTITY_UID to personParentJoin.ppjUid.toString())
        val presenter = ParentalConsentManagementPresenter(context,
            presenterArgs, mockView, mockLifecycleOwner, di)
        presenter.onCreate(null)

        val initialEntity = mockView.captureLastEntityValue(5000)!!
        initialEntity.ppjStatus = PersonParentJoin.STATUS_APPROVED

        presenter.handleClickSave(initialEntity)

        val systemImpl: UstadMobileSystemImpl by di.instance()
        verify(mockView, timeout(5000 ).atLeastOnce()).relationshipFieldError =
            eq(systemImpl.getString(MessageID.field_required_prompt, context))
    }


}