package com.ustadmobile.core.controller

import com.nhaarman.mockitokotlin2.*
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.impl.UMStorageDir
import com.ustadmobile.core.impl.UmResultCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import com.ustadmobile.core.util.UstadTestRule
import com.ustadmobile.core.util.activeRepoInstance
import com.ustadmobile.core.util.directActiveDbInstance
import com.ustadmobile.core.util.directActiveRepoInstance
import com.ustadmobile.core.util.ext.captureLastEntityValue
import com.ustadmobile.core.view.ContentEntryEdit2View
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.*
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton


class PersonEditPresenterTest  {

    @JvmField
    @Rule
    var ustadTestRule = UstadTestRule()

    private lateinit var mockView: PersonEditView

    private lateinit var context: Any

    private lateinit var db: UmAppDatabase

    private lateinit var repo: UmAppDatabase

    private lateinit var mockLifecycleOwner: DoorLifecycleOwner

    private val parentUid: Long = 12345678L

    private val timeoutInMill: Long = 5000

    private lateinit var mockPersonDao:PersonDao

    private val errorMessage: String = "Dummy error"

    private lateinit var systemImpl: UstadMobileSystemImpl

    private lateinit var di: DI


    @Before
    fun setUp() {
        context = Any()
        mockView = mock{}
        mockLifecycleOwner = mock { }

        systemImpl = mock{
            on { getStorageDirs(any(), any()) }.thenAnswer {
                (it.getArgument(1) as UmResultCallback<List<UMStorageDir>>).onDone(
                        mutableListOf(UMStorageDir("", "", removableMedia = false,
                        isAvailable = false, isUserSpecific = false)))
            }
            on { getString(any(), any()) }.thenAnswer{errorMessage}
        }


        di = DI {
            import(ustadTestRule.diModule)
            bind<UstadMobileSystemImpl>(overrides = true) with singleton { systemImpl }
        }

        db = di.directActiveDbInstance()
        repo = di.directActiveRepoInstance()

        val systemImpl: UstadMobileSystemImpl by di.instance()


        val repo: UmAppDatabase by di.activeRepoInstance()
        mockPersonDao = spy(repo.personDao)
        whenever(repo.personDao).thenReturn(mockPersonDao)
    }

    @After
    fun tearDown() {}

    private fun createPerson(): Person {
        return Person().apply {
            fatherName = "Doe"
            firstNames = "Jane"
            lastName = "Doe"
        }
    }

    @Test
    fun givenPresenterCreatedInRegistrationMode_whenUsernameAndPasswordNotFilledClickSave_shouldShowErrors() {
        val args = mapOf(UstadView.ARG_WORKSPACE to Json.stringify(WorkSpace.serializer(), WorkSpace().apply {
            registrationAllowed = true
        }))

        val person = createPerson()
        val presenter = PersonEditPresenter(context, args,mockView, di,mockLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleClickSave(person)

        argumentCaptor<Boolean>().apply {
            verify(mockView, timeout(timeoutInMill)).showRequiredPasswordError = capture()
            verify(mockView, timeout(timeoutInMill)).showUsernameError = capture()
            assertEquals("Required password field errors were shown", true, firstValue)
            assertEquals("Required username field errors were shown", true, secondValue)
        }

    }

    @Test
    fun givenPresenterCreatedInRegistrationMode_whenPasswordAndConfirmPasswordDoesNotMatchClickSave_shouldShowErrors() {
        val args = mapOf(UstadView.ARG_WORKSPACE to Json.stringify(WorkSpace.serializer(), WorkSpace().apply {
            registrationAllowed = true
        }))

        val person = createPerson().apply {
            username =""
        }
        mockView.password = "password"
        mockView.confirmedPassword = "password1"
        val presenter = PersonEditPresenter(context, args,mockView, di,mockLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleClickSave(person)

        argumentCaptor<Boolean>().apply {
            verify(mockView, timeout(timeoutInMill)).showPasswordMatchingError = capture()
            assertEquals("Password doesn't match field errors were shown", true, firstValue)
        }

    }


    @Test
    fun givenPresenterCreatedInRegistrationMode_whenFormFilledAndClickSave_shouldRegisterAPerson() {
        val args = mapOf(UstadView.ARG_WORKSPACE to Json.stringify(WorkSpace.serializer(), WorkSpace().apply {
            registrationAllowed = true
        }))

        val person = createPerson()
        mockView.password = "password"
        mockView.confirmedPassword = "password"
        val presenter = PersonEditPresenter(context, args,mockView, di,mockLifecycleOwner)

        presenter.onCreate(null)

        presenter.handleClickSave(person)

        argumentCaptor<Boolean>().apply {
            verify(mockView, timeout(timeoutInMill)).showPasswordMatchingError = capture()
            assertEquals("Password doesn't match field errors were shown", true, firstValue)
        }

    }

}